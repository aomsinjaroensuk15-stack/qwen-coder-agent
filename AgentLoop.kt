package dev.qwen.coderagent.agent

import org.json.JSONArray
import org.json.JSONObject

/**
 * วงจรหลักของ agent: รับ task → โมเดลตอบ (อาจขอเรียก tool) → รันจริง → แนบผล → วนซ้ำ
 * ออกแบบให้ provider-agnostic — เปลี่ยนโมเดล/quant ทีหลังไม่ต้องแตะ loop นี้
 */
class AgentLoop(
    private val llm: LlmClient,
    private val tools: ToolRegistry,
    private val maxTurns: Int = 10
) {
    private val memory = Memory()

    init {
        memory.add("system", SYSTEM_PROMPT)
    }

    /** รับข้อความจาก user แล้ววน loop จนโมเดลตอบสรุป (หรือครบ maxTurns) */
    fun chat(userInput: String, onToken: (String) -> Unit): String {
        memory.add("user", userInput)

        for (turn in 1..maxTurns) {
            val response = llm.generate(buildPrompt())
            val parsed = parseResponse(response)

            when {
                parsed.toolCall != null -> {
                    memory.add("assistant", response)
                    val result = tools.execute(parsed.toolCall)
                    memory.add("tool", result)
                }
                else -> {
                    memory.add("assistant", response)
                    onToken(response)
                    return response
                }
            }
        }
        return "(หมดจำนวนรอบสูงสุด $maxTurns turns — ลองถามใหม่แบบแบ่งเล็กลง)"
    }

    private fun buildPrompt(): String {
        // TODO: ใช้ chat template ของ Qwen2.5 (ChatML format) + ฝัง tools schema
        return memory.toJson().toString()
    }

    /** แยก tool_call จากคำตอบโมเดล — Qwen2.5-Coder มักตอบเป็น JSON ใน <tool_call> tags */
    private fun parseResponse(response: String): Parsed {
        val m = Regex("<tool_call>(.*?)</tool_call>", RegexOption.DOT_MATCHES_ALL).find(response)
        return if (m != null) {
            try {
                Parsed(toolCall = JSONObject(m.groupValues[1]))
            } catch (e: Exception) { Parsed(text = response) }
        } else Parsed(text = response)
    }

    fun close() = llm.close()

    private data class Parsed(val text: String? = null, val toolCall: JSONObject? = null)

    companion object {
        private const val SYSTEM_PROMPT = """You are Qwen Coder Agent, an offline AI coding assistant running on Android.
You help the user with programming tasks and explain concepts with diagrams.
When a concept would be clearer visually, CREATE a diagram using the create_mermaid tool (flowchart, sequenceDiagram, mindmap, erDiagram...).
Always wrap tool calls in <tool_call>{"name": "...", "arguments": {...}}</tool_call>.
Be concise. Answer in the same language the user uses."""
    }
}

/** abstraction สำหรับโมเดล — LlamaBridge implement ตัวนี้ */
interface LlmClient {
    fun generate(prompt: String): String
    fun close()
}
