package dev.qwen.coderagent.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.qwen.coderagent.agent.AgentLoop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatMessage(val role: String, val content: String, val isDiagram: Boolean = false)

class ChatViewModel(private val agent: AgentLoop) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking

    fun send(input: String) {
        if (input.isBlank()) return
        _messages.value = _messages.value + ChatMessage("user", input)
        _isThinking.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val reply = agent.chat(input) { /* stream token ทีละชิ้นได้ที่นี่ */ }
            _messages.value = _messages.value + ChatMessage(
                role = "assistant",
                content = reply,
                isDiagram = reply.contains("```mermaid")
            )
            _isThinking.value = false
        }
    }
}
