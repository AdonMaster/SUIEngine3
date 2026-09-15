package app.adon.suiengine.renderer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.adon.suiengine.ast.Lexer
import app.adon.suiengine.ast.Parser
import app.adon.suiengine.renderer.components.DialogError
import app.adon.suiengine.renderer.resource.Resource

class SUIEngine {

    //
    val resource = Resource()

    //
    @Composable
    fun Render(payload: String) {

        // initializing nodes
        val (nodes, err) = remember(payload) {
            runCatching {
                val lexer = Lexer(payload)
                val parser = Parser(lexer.tokenize())
                Pair(parser.parse(), null)
            }.getOrElse {
                Pair(emptyList(), it.message ?: "unkown error:341")
            }
        }

        // invoker
        InvokeGroup(nodes)

        // dialog error
        var showErrorDialog by remember(err) { mutableStateOf(err != null) }
        if (err != null && showErrorDialog) {
            DialogError(
                message = err,
                onDismiss = {
                    showErrorDialog = false
                }
            )
        }

    }

}