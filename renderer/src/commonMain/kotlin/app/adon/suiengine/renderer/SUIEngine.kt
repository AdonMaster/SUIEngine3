package app.adon.suiengine.renderer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

        //
        LaunchedEffect(payload) {
            vm.parseIfNeeded(payload)
        }

        val nodes by vm.nodes.collectAsStateWithLifecycle()
        val parseErr by vm.parseError.collectAsStateWithLifecycle()

        val rootContext = remember(vm) {
            Context("root", null, vm)
        }

        //
        if (nodes.isNotEmpty()) {
            FunctionRegistry.InvokeGroup(
                nodes = nodes,
                context = rootContext
            )
        }

        //
        var showErrorDialog by remember(parseErr) { mutableStateOf(parseErr != null) }
        if (parseErr != null && showErrorDialog) {
            DialogErrorStack(
                errors = listOf("from root", parseErr!!),
                onDismiss = { showErrorDialog = false }
            )
        }

        //
        val renderErrors by vm.errors.collectAsStateWithLifecycle()
        if (renderErrors.isNotEmpty()) {
            DialogErrorStack(renderErrors.asReversed()) {
                vm.setErrors(emptyList())
            }
        }

    }

}