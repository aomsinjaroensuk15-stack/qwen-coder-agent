package dev.qwen.coderagent.llm

import android.content.Context
import java.io.File

/** จัดการไฟล์โมเดล GGUF — คัดลอกจาก assets ครั้งแรก หรือเช็กไฟล์ที่ push เข้ามา */
class ModelManager(private val context: Context) {

    companion object {
        const val MODEL_FILE = "qwen2.5-coder-3b-instruct-q4_k_m.gguf"
        const val MODEL_URL = "https://huggingface.co/Qwen/Qwen2.5-Coder-3B-Instruct-GGUF"
    }

    /** path ที่โมเดลควรอยู่ — ใน app-specific storage (ไม่ต้องขอ permission) */
    fun modelPath(): File = File(context.filesDir, "model/$MODEL_FILE")

    fun isModelReady(): Boolean = modelPath().exists() && modelPath().length() > 1_000_000_000L

    /** TODO: ดาวน์โหลดจาก Hugging Face ด้วย DownloadManager + แสดง progress */
}
