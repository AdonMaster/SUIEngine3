package app.adon.suiengine.ast

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ASTPlayground() {

    var textInput by remember {
        mutableStateOf(exampleTextFull)
    }
    val (astResult, errorResult) = remember(textInput) {
        try {
            val tokens = Lexer(textInput).tokenize()
            val nodes = Parser(tokens).parse()
            Pair(nodes, null)
        } catch (e: Exception) {
            Pair(emptyList(), e.message ?: "Erro desconhecido")
        }
    }

    MaterialTheme {

        Scaffold { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Top(astResult, errorResult)
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Bot(textInput) { textInput = it }
                }
            }
        }
    }
}


@Composable
private fun Top(astResult: List<Node>, errorResult: String?) {
    // tokens
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        RenderItems(astResult)
    }

    // error
    errorResult?.let {
        Text(
            errorResult,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.errorContainer),
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

@Composable
private fun RenderItems(nodes: List<Node>, depth: Int = 0) {
    nodes.forEach { node ->
        RenderItem(node, depth)
    }
}

@Composable
private fun RenderItem(node: Node, depth: Int) {
    when (node) {
        is Node.Arr -> {
            Txt("arr:", depth)
            RenderItems(node.v, depth + 1)
        }

        is Node.Fn -> {
            Txt("fn: ${node.name}", depth)
            Txt("-params", depth)
            RenderItems(node.params, depth + 1)
            Txt("-children", depth)
            RenderItems(node.children, depth + 1)
        }

        is Node.Dict -> {
            Txt("dict", depth)
        }

        is Node.Param -> {
            Txt("param(${node.name})", depth)
            RenderItem(node.value, depth + 1)
        }

        else -> {
            Txt(node.stringableVal(), depth)
        }
    }
}

@Composable
private fun Txt(s: String, depth: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(depth) {
            Text("•", style = MaterialTheme.typography.labelMedium)
        }
        Text(s, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun Bot(text: String, onText: (String) -> Unit) {

    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp)
    ) {
        Text(
            text = "✍️ Editor de Código",
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = text,
            onValueChange = onText,
            modifier = Modifier.fillMaxWidth().weight(1f),
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            ),
            placeholder = { Text("Digite seu código aqui...") }
        )

        Button(
            onClick = { onText("") }
        ) {
            Text("clear")
        }
    }
}
