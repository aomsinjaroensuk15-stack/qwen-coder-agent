package dev.qwen.coderagent.tools

import dev.qwen.coderagent.agent.Tool
import dev.qwen.coderagent.agent.ToolResult
import org.json.JSONObject
import java.io.File

/** 🌟 ฟีเจอร์เด่นของเรา — agent สร้าง diagram เพื่ออธิบาย โดยไม่ต้องมี image model */

object DiagramTools {

    class CreateMermaid(private val root: File) : Tool {
        override val name = "create_mermaid"
        override val description = """สร้างไฟล์ markdown ที่มี Mermaid diagram เพื่ออธิบายแนวคิด
ใช้เมื่อคำอธิบายซับซ้อน: flowchart TD/LR, sequenceDiagram, erDiagram, mindmap, gantt, classDiagram
ตัวอย่าง: ```mermaid
flowchart TD
  A[เริ่ม] --> B{เงื่อนไข}
  B -->|จริง| C[ทำ A]
```"""
        override val parametersJson = """{"type":"object","properties":{"filename":{"type":"string","description":"เช่น architecture.md"},"diagram_type":{"type":"string"},"content":{"type":"string","description":"Mermaid code ภายใน ```mermaid fence"}},"required":["filename","diagram_type","content"]}"""

        override fun execute(args: JSONObject): ToolResult {
            val diagramType = args.getString("diagram_type")
            val content = args.getString("content")
            val md = buildString {
                appendLine("# Diagram: $diagramType")
                appendLine("```mermaid")
                appendLine(content)
                appendLine("```")
            }
            val f = File(root, "diagrams/${args.getString("filename")}")
            f.parentFile?.mkdirs()
            f.writeText(md)
            return ToolResult(true, "สร้าง diagram แล้ว: ${f.path}
${validate(content)}")
        }
    }

    class ValidateDiagram : Tool {
        override val name = "validate_diagram"
        override val description = "ตรวจ syntax เบื้องต้นของ Mermaid code (offline — ตรวจโครงสร้าง node/edge พื้นฐาน)"
        override val parametersJson = """{"type":"object","properties":{"content":{"type":"string"}},"required":["content"]}"""

        override fun execute(args: JSONObject): ToolResult {
            val result = validate(args.getString("content"))
            return ToolResult(result.startsWith("OK"), result)
        }
    }

    /** validation แบบ offline — ตรวจโครงสร้างพื้นฐาน ป้องกัน syntax พังก่อน render */
    internal fun validate(content: String): String {
        val errors = mutableListOf<String>()
        if (!content.trimStart().startsWith("flowchart") &&
            !content.trimStart().startsWith("sequenceDiagram") &&
            !content.trimStart().startsWith("graph") &&
            !content.trimStart().startsWith("erDiagram") &&
            !content.trimStart().startsWith("mindmap") &&
            !content.trimStart().startsWith("classDiagram") &&
            !content.trimStart().startsWith("gantt")
        ) errors.add("ไม่พบประเภท diagram ที่รองรับ (flowchart/sequenceDiagram/erDiagram/mindmap/classDiagram/gantt)")

        val openBrackets = content.count { it == '[' || it == '{' }
        val closeBrackets = content.count { it == ']' || it == '}' }
        if (openBrackets != closeBrackets) errors.add("วงเล็บไม่ครบคู่: เปิด $openBrackets ปิด $closeBrackets")

        if (content.lines().any { it.contains("-->") && !it.contains("[") && !it.contains("(") })
            errors.add("พบ edge ที่ไม่มี node — ตรวจสอบรูปแบบ A --> B")

        return if (errors.isEmpty()) "OK: diagram ผ่านการตรวจพื้นฐาน"
        else "พบปัญหา: ${errors.joinToString("; ")}"
    }
}
