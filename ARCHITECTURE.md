# สถาปัตยกรรมระบบ

```mermaid
flowchart TD
    U[ผู้ใช้ พิมพ์คำถาม] --> UI[ChatScreen - Compose]
    UI --> VM[ChatViewModel]
    VM --> LOOP[AgentLoop]
    LOOP --> MEM[(Memory - context 8K tokens)]
    LOOP --> LLM[LlamaBridge - JNI]
    LLM --> GGUF[Qwen2.5-Coder-3B GGUF ~2GB]
    LOOP -->|ถ้าโมเดลขอ tool| REG[ToolRegistry]
    REG --> FT[FileTools - read/write sandbox]
    REG --> DT[DiagramTools - create/validate mermaid]
    DT --> MD[diagrams/*.md]
    LLM -->|ผลลัพธ์| VM
    VM --> UI
```

## หลักการออกแบบ
1. **Offline ทั้งหมด** — ไม่มี network call ใน agent path
2. **Provider-agnostic** — เปลี่ยนโมเดลภายหลังได้โดยแก้แค่ LlamaBridge
3. **Sandbox file access** — agent เข้าถึงได้เฉพาะโฟลเดอร์ของแอป
4. **Validation loop** — DiagramTools ตรวจ syntax พื้นฐานก่อน save เสมอ
