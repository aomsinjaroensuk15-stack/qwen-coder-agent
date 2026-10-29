// JNI bridge ระหว่าง Kotlin กับ llama.cpp C API
// ต้องมี llama.cpp เป็น submodule ในโฟลเดอร์เดียวกัน (ดู README)
#include <jni.h>
#include <string>
#include "llama.h"

extern "C" JNIEXPORT jlong JNICALL
Java_dev_qwen_coderagent_llm_LlamaBridge_nativeLoadModel(
        JNIEnv *env, jobject thiz, jstring path, jint n_ctx, jint n_threads) {
    const char *model_path = env->GetStringUTFChars(path, nullptr);

    llama_backend_init();
    llama_model_params mparams = llama_model_default_params();
    llama_model *model = llama_load_model_from_file(model_path, mparams);
    env->ReleaseStringUTFChars(path, model_path);
    if (!model) return 0;

    llama_context_params cparams = llama_context_default_params();
    cparams.n_ctx = n_ctx;
    cparams.n_threads = n_threads;
    llama_context *ctx = llama_new_context_with_model(model, cparams);
    return reinterpret_cast<jlong>(ctx);
}

extern "C" JNIEXPORT jstring JNICALL
Java_dev_qwen_coderagent_llm_LlamaBridge_nativeGenerate(
        JNIEnv *env, jobject thiz, jlong ctx_ptr, jstring prompt, jint max_tokens, jfloat temp) {
    llama_context *ctx = reinterpret_cast<llama_context *>(ctx_ptr);
    // TODO: tokenize prompt (รวม ChatML template), llama_decode loop, sampling
    // ตัวอย่างโครง:
    //   auto tokens = llama_tokenize(...);
    //   while (n < max_tokens) { llama_decode(ctx, batch); sample; break on EOS; }
    std::string result = ""; // placeholder — implement sampling loop
    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_dev_qwen_coderagent_llm_LlamaBridge_nativeFree(JNIEnv *env, jobject thiz, jlong ctx_ptr) {
    llama_free(reinterpret_cast<llama_context *>(ctx_ptr));
    llama_backend_free();
}
