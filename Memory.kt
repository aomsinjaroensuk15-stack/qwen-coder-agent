package dev.qwen.coderagent.agent

import org.json.JSONArray
import org.json.JSONObject

/** ข้อความใน conversation — role: system/user/assistant/tool */
data class Message(val role: String, val content: String)

/**
 * จัดการ context window แบบง่ายสำหรับเครื่อง 8GB:
 * - เก็บ system prompt เต็มเสมอ
 * - ถ้าเกิน tokenBudget จะตัดข้อความเก่าจากกลาง (เก็บ system + ล่าสุดไว้)
 */
class Memory(private val tokenBudget: Int = 6000) {
    private val messages = mutableListOf<Message>()

    fun add(role: String, content: String) {
        messages.add(Message(role, content))
        compact()
    }

    fun all(): List<Message> = messages.toList()

    /** ตัดข้อความเก่าออกเมื่อ context ใกล้เต็ม — ประมาณ 1 token ≈ 4 ตัวอักษร */
    private fun compact() {
        var total = messages.sumOf { it.content.length / 4 + 8 }
        while (total > tokenBudget && messages.size > 2) {
            messages.removeAt(1) // เก็บ system (index 0) เสมอ
            total = messages.sumOf { it.content.length / 4 + 8 }
        }
    }

    /** แปลงเป็น JSON string สำหรับสร้าง prompt */
    fun toJson(): JSONArray = JSONArray().apply {
        messages.forEach { put(JSONObject().apply { put("role", it.role); put("content", it.content) }) }
    }
}
