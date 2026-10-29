package dev.qwen.coderagent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.qwen.coderagent.agent.AgentLoop
import dev.qwen.coderagent.agent.ToolRegistry
import dev.qwen.coderagent.llm.LlamaBridge
import dev.qwen.coderagent.tools.DiagramTools
import dev.qwen.coderagent.tools.FileTools
import dev.qwen.coderagent.ui.ChatScreen
import dev.qwen.coderagent.ui.ChatViewModel

class MainActivity : ComponentActivity() {

    private lateinit var agent: AgentLoop

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // --- ประกอบระบบ (DI แบบ manual) ---
        val llm = LlamaBridge()
        val registry = ToolRegistry().apply {
            register(FileTools.ReadFile())
            register(FileTools.WriteFile())
            register(DiagramTools.CreateMermaid())
            register(DiagramTools.ValidateDiagram())
        }
        agent = AgentLoop(llm, registry)

        setContent {
            ChatScreen(viewModel = ChatViewModel(agent))
        }
    }

    override fun onDestroy() {
        agent.close()
        super.onDestroy()
    }
}
