package dev.qwen.coderagent.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/** หน้าแชทหลัก — แสดง mermaid code block แบบ highlight เฉยๆ (render จริงทำทีหลังด้วย WebView) */
@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    var input by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsState()
    val isThinking by viewModel.isThinking.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) { listState.animateScrollToItem(messages.size) }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.role == "user"
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = if (isUser) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.widthIn(max = 320.dp)
                    ) {
                        Column(Modifier.padding(10.dp)) {
                            if (msg.isDiagram) {
                                Text("📊 มี diagram แนบ", style = MaterialTheme.typography.labelMedium)
                            }
                            Text(msg.content)
                        }
                    }
                }
            }
            if (isThinking) item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    Text("กำลังคิด…", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("ถามเรื่องโค้ด หรือขอให้อธิบายด้วย diagram…") },
                enabled = !isThinking
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { viewModel.send(input); input = "" },
                enabled = !isThinking && input.isNotBlank()
            ) { Text("ส่ง") }
        }
    }
}
