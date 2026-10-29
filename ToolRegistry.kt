package dev.qwen.coderagent.agent

import org.json.JSONArray
import org.json.JSONObject

/** คลัง tool ของ agent — dispatch การเรียกใช้ + สร้าง schema ให้โมเดล */
class ToolRegistry {
    private val tools = mutableMapOf<String, Tool>()

    fun register(tool: Tool) { tools[tool.name] = tool }

    fun get(name: String): Tool? = tools[name]

    /** รายการ schema ทั้งหมด สำหรับฝังใน prompt (tools section) */
    fun schemasJson(): JSONArray = JSONArray().apply {
        tools.values.forEach { t ->
            put(JSONObject().apply {
                put("type", "function")
                put("function", JSONObject().apply {
                    put("name", t.name)
                    put("description", t.description)
                    put("parameters", JSONObject(t.parametersJson))
                })
            })
        }
    }

    /** เรียก tool จากคำสั่งที่โมเดลส่งมา คืนผลเป็น string สำหรับแนบใน context */
    fun execute(callJson: JSONObject): String {
        val name = callJson.optString("name")
        val args = callJson.optJSONObject("arguments") ?: JSONObject()
        val tool = tools[name] ?: return """{"error": "unknown tool: $name"}"""
        return try {
            val r = tool.execute(args)
            JSONObject().apply {
                put("ok", r.ok)
                put("output", r.output.take(4000)) // ตัด context กันพอง
            }.toString()
        } catch (e: Exception) {
            """{"ok": false, "output": "${e.message}"}"""
        }
    }
}
