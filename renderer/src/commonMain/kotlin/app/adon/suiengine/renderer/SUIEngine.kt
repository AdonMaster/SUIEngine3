package app.adon.suiengine.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.adon.suiengine.renderer.components.DialogErrorStack
import app.adon.suiengine.renderer.components.TextError
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.renderer.RenderEntry
import app.adon.suiengine.renderer.state.DataState

@Composable
fun SUIEngine(
    rawCode: String,
    vm: SUIEngineVM = viewModel(key = rawCode) { SUIEngineVM(rawCode) }
) {

    val nodesState by vm.nodes.collectAsStateWithLifecycle()
    val errorStack by vm.errStack.collectAsStateWithLifecycle()

    //
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when(val state = nodesState) {
            DataState.Idle -> {}
            DataState.Loading -> {
                CircularProgressIndicator(modifier = Modifier)
            }
            is DataState.Error -> {
                TextError(state.reason)
            }
            is DataState.Success -> {
                RenderEntry(
                    state.payload,
                    context = Context("root", null, vm)
                )
            }
        }
    }

    if (errorStack.isNotEmpty()) {
        DialogErrorStack(errorStack, onDismiss = { vm.clearErrStack() })
    }

}
