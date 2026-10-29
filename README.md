# Qwen Coder Agent 📱🤖

AI Coding Agent บน Android รันโมเดล **Qwen2.5-Coder-3B-Instruct** แบบ offline บนเครื่อง
ออกแบบสำหรับเครื่อง 8GB RAM — โมเดล GGUF Q4_K_M (~2GB)

## ฟีเจอร์หลัก
- 🤖 Agent loop: plan → tool call → observe (เขียนเองทั้งหมด ไม่ใช่ framework)
- 📊 Diagram mode: agent สร้าง Mermaid/SVG เพื่ออธิบาย ฝังใน markdown
- 🔒 Offline 100% — ไม่มีการเรียก API ภายนอก
- 🧠 Model: Qwen2.5-Coder-3B-Instruct GGUF (Q4_K_M)

## โครงสร้างโปรเจกต์
```
app/src/main/
├── java/dev/qwen/coderagent/
│   ├── MainActivity.kt          # จุดเข้าแอป
│   ├── agent/                   # 🧠 สมองของ agent
│   │   ├── AgentLoop.kt         # วงจรหลัก plan→tool→observe
│   │   ├── Tool.kt              # interface ของ tool
│   │   ├── ToolRegistry.kt      # ลงทะเบียน tool
│   │   └── Memory.kt            # จัดการ context/history
│   ├── llm/                     # 🔌 ต่อกับโมเดล
│   │   ├── LlamaBridge.kt       # JNI wrapper (Kotlin ↔ C)
│   │   └── ModelManager.kt      # จัดการไฟล์โมเดล
│   ├── tools/                   # 🛠️ มือของ agent
│   │   ├── FileTools.kt         # อ่าน/เขียนไฟล์ใน app storage
│   │   └── DiagramTools.kt      # สร้าง/validate Mermaid, SVG
│   └── ui/                      # 🎨 หน้าตาแอป (Jetpack Compose)
│       ├── ChatScreen.kt
│       └── ChatViewModel.kt
└── cpp/
    └── llama_bridge.cpp         # JNI → llama.cpp C API
```

## การตั้งค่า (Setup)

### 1. Clone + เตรียม llama.cpp
```bash
git submodule add https://github.com/ggml-org/llama.cpp app/src/main/cpp/llama.cpp
```

### 2. ดาวน์โหลดโมเดล
ไปที่ Hugging Face: `Qwen/Qwen2.5-Coder-3B-Instruct-GGUF`
เลือกไฟล์ `qwen2.5-coder-3b-instruct-q4_k_m.gguf` (~2GB)
แล้ว push เข้าเครื่อง:
```bash
adb push qwen2.5-coder-3b-instruct-q4_k_m.gguf /sdcard/Android/data/dev.qwen.coderagent/files/model.gguf
```
(หรือใส่ใน app แล้วให้ ModelManager คัดลอกจาก assets ครั้งแรก)

### 3. Build & Run
เปิดใน Android Studio → Sync → Run

## ข้อจำกัดที่รู้ไว้ (เครื่อง 8GB)
- ใช้ context ~8K tokens ต่อรอบ (ถนอม RAM)
- 1 task ควรจบภายใน ~10 turns
- โมเดล 3B — งาน syntax (Mermaid/SVG) ทำดี งาน reasoning หนักๆ ต้องอาศัย validation loop ช่วย
