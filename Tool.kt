package dev.qwen.coderagent.agent

import org.json.JSONObject

/** ผลลัพธ์จากการเรียก tool — จะถูกส่งกลับให้โมเดลอ่านในรอบถัดไป */
data class ToolResult(val ok: Boolean, val output: String)

/** ทุก tool ต้อง implement interface นี้ — จะเพิ่ม tool ใหม่ก็แค่สร้าง class แล้ว register */
interface Tool {
    val name: String
    val description: String
    val parametersJson: String  // JSON Schema (OpenAI tools format)

    fun execute(args: JSONObject): ToolResult
}
