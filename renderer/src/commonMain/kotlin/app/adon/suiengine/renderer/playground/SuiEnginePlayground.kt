package app.adon.suiengine.renderer.playground

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.adon.suiengine.ast.Lexer
import app.adon.suiengine.ast.Node
import app.adon.suiengine.ast.Parser
import app.adon.suiengine.ast.normalizeIfChains
import app.adon.suiengine.renderer.components.TextError

@Composable
fun SuiEnginePlayground(rawCode: String) {
    val textState = remember(key1 = rawCode) { TextFieldState(rawCode) }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Top(textState)
        Bottom(textState)

    }
}

@Composable
private fun ColumnScope.Bottom(textState: TextFieldState) {
    BasicTextField(
        modifier = Modifier
            .weight(1f)
            .background(MaterialTheme.colorScheme.surfaceDim)
            .fillMaxWidth()
        ,
        state = textState
    )
}

@Composable
private fun ColumnScope.Top(textState: TextFieldState) {

    var err by remember {  mutableStateOf("") }
    val nodes by produceState(initialValue = emptyList(), key1 = textState) {
        snapshotFlow { textState.text.toString() }
            .collect { currentText ->
                runCatching {
                    val lexer = Lexer(currentText)
                    val parser = Parser(lexer.tokenize())
                    parser.parse()
                        .normalizeIfChains()
                }.onSuccess { parsedNodes ->
                    err = ""
                    value = parsedNodes
                }.onFailure { throwable ->
                    err = throwable.message ?: "Err@123"
                    value = emptyList()
                }
            }
    }

    Column(
        modifier = Modifier
            .weight(1f)
            .padding(20.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
        ,
    ) {
        if (err.isNotEmpty()) {
            TextError(err)
        } else {
            RenderNode(nodes)
        }
    }
}


@Composable
private fun RenderNode(nodes: List<Node>) {
    for (node in nodes) {
        when (node) {
            is Node.Str -> {
                Text("\"${node.v}\"")
                node.extension?.let { ext -> ColLine { Text("ext:"); RenderNode(listOf(ext)) } }
            }
            is Node.Var -> {
                Text("$${node.name}."+ node.segments.joinToString(".") { it.str() })
                node.extension?.let { ext -> ColLine { Text("ext:"); RenderNode(listOf(ext)) } }
            }
            is Node.Bool -> {
                Text(node.v.toString())
                node.extension?.let { ext -> ColLine { Text("ext:"); RenderNode(listOf(ext)) } }
            }
            is Node.Number -> {
                Text(node.v.toString())
                node.extension?.let { ext -> ColLine { Text("ext:"); RenderNode(listOf(ext)) } }
            }
            Node.Null -> Text("null")
            is Node.Arr -> {
                Text("[")
                ColLine {
                    RenderNode(node.v)
                }
                Text("]")
                node.extension?.let { ext -> ColLine { Text("ext:"); RenderNode(listOf(ext)) } }

            }
            is Node.Dict -> {
                Text("{")
                node.v.entries.forEach { entry ->
                    Text(entry.key)
                    ColLine {
                        RenderNode(listOf(entry.value))
                    }
                }
                Text("}")
                node.extension?.let { ext -> ColLine { Text("ext:"); RenderNode(listOf(ext)) } }

            }
            is Node.Fn -> {
                Text(node.name)
                node.params.takeIf { it.isNotEmpty() }?.let {
                    Text("(")
                    ColLine {
                        RenderNode(node.params)
                    }
                    Text(")")
                }
                node.children.takeIf { it.isNotEmpty() }?.let {
                    Text("{")
                    ColLine {
                        RenderNode(node.children)
                    }
                    Text("}")
                }
                node.extension?.let { ext -> ColLine { Text("ext:"); RenderNode(listOf(ext)) } }
            }
            is Node.Param -> {
                Text("${node.name}")
                ColLine {
                    RenderNode(listOf(node.value))
                }
            }
        }
    }
}


@Composable
private fun ColLine(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val strokeWidth = 4.dp.toPx() // Espessura da borda
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f), // Cor da borda
                    start = Offset(x = strokeWidth / 2, y = 0f),
                    end = Offset(x = strokeWidth / 2, y = size.height),
                    strokeWidth = strokeWidth
                )
            }
            .padding(start = 16.dp)
    ) {
        content()
    }
}