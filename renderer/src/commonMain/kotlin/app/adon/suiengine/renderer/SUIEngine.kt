package app.adon.suiengine.renderer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import app.adon.suiengine.ast.Lexer
import app.adon.suiengine.ast.Parser
import app.adon.suiengine.renderer.components.DialogError
import app.adon.suiengine.renderer.components.DialogErrorStack
import app.adon.suiengine.renderer.resource.Resource

class SUIEngine {

    //
    val resource = Resource()

    //
    @Composable
    fun Render(payload: String) {

        val vm: SUIEngineVM = viewModel { SUIEngineVM() }
        val rootContext = remember(payload, vm) {
            Context("root", null, vm)
        }

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
        FunctionRegistry.InvokeGroup(
            nodes = nodes,
            context = rootContext
        )

        // parsing error
        var showErrorDialog by remember(err) { mutableStateOf(err != null) }
        if (err != null && showErrorDialog) {
            DialogErrorStack(
                errors = listOf("from root", err),
                onDismiss = {
                    showErrorDialog = false
                }
            )
        }

        // render error
        val renderErrors by vm.errors.collectAsState()
        if (renderErrors.isNotEmpty()) {
            DialogErrorStack(renderErrors.asReversed()) {
                vm.setErrors(emptyList())
            }
        }

    }

}