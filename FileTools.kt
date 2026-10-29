package dev.qwen.coderagent.tools

import dev.qwen.coderagent.agent.Tool
import dev.qwen.coderagent.agent.ToolResult
import org.json.JSONObject
import java.io.File

/** อ่าน/เขียนไฟล์เฉพาะใน app storage — จำกัด root เพื่อความปลอดภัย (sandbox) */
object FileTools {

    class ReadFile(private val root: File) : Tool {
        override val name = "read_file"
        override val description = "อ่านเนื้อหาไฟล์ใน workspace ของ agent"
        override val parametersJson = """{"type":"object","properties":{"path":{"type":"string"},"limit":{"type":"integer","description":"จำนวนบรรทัดสูงสุด"}},"required":["path"]}"""

        override fun execute(args: JSONObject): ToolResult {
            val f = safeResolve(args.getString("path")) ?: return ToolResult(false, "path ไม่ถูกต้อง")
            if (!f.exists()) return ToolResult(false, "ไม่มีไฟล์นี้")
            val limit = args.optInt("limit", 500)
            val text = f.readLines().take(limit).joinToString("
")
            return ToolResult(true, text)
        }

        private fun safeResolve(p: String): File? {
            val f = File(root, p).canonicalFile
            return if (f.path.startsWith(root.canonicalPath)) f else null
        }
    }

    class WriteFile(private val root: File) : Tool {
        override val name = "write_file"
        override val description = "เขียน/สร้างไฟล์ใน workspace ของ agent (ใช้เก็บโค้ด หรือ diagram .md)"
        override val parametersJson = """{"type":"object","properties":{"path":{"type":"string"},"content":{"type":"string"}},"required":["path","content"]}"""

        override fun execute(args: JSONObject): ToolResult {
            val f = File(root, args.getString("path")).canonicalFile
            if (!f.path.startsWith(root.canonicalPath)) return ToolResult(false, "path ไม่ถูกต้อง")
            f.parentFile?.mkdirs()
            f.writeText(args.getString("content"))
            return ToolResult(true, "เขียนไฟล์แล้ว: ${f.name} (${f.length()} bytes)")
        }
    }
}
