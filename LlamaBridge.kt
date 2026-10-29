package dev.qwen.coderagent.llm

import dev.qwen.coderagent.agent.LlmClient

/**
 * สะพาน Kotlin → llama.cpp ผ่าน JNI
 * โหลดโมเดล GGUF ครั้งเดียว แล้วเรียก generate ซ้ำได้
 */
class LlamaBridge : LlmClient {

    private var contextPtr: Long = 0

    init { System.loadLibrary("llama-bridge") }

    external fun nativeLoadModel(path: String, nCtx: Int, nThreads: Int): Long
    external fun nativeGenerate(ctx: Long, prompt: String, maxTokens: Int, temperature: Float): String
    external fun nativeFree(ctx: Long)

    /** โหลดโมเดล — เรียกครั้งเดียวตอนเปิดแอป */
    fun loadModel(path: String) {
        contextPtr = nativeLoadModel(path, nCtx = 8192, nThreads = 4)
        if (contextPtr == 0L) throw RuntimeException("โหลดโมเดลไม่สำเร็จ: $path")
    }

    override fun generate(prompt: String): String {
        check(contextPtr != 0L) { "ยังไม่ได้โหลดโมเดล" }
        return nativeGenerate(contextPtr, prompt, maxTokens = 1024, temperature = 0.3f)
    }

    override fun close() {
        if (contextPtr != 0L) { nativeFree(contextPtr); contextPtr = 0 }
    }
}
