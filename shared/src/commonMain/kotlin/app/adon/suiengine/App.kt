package app.adon.suiengine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.adon.suiengine.data.DataState
import app.adon.suiengine.renderer.SUIEngine
import suiengine.shared.generated.resources.Res

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun App(
    vm: AppVM = viewModel { AppVM() }
) {

    val state by vm.state.collectAsStateWithLifecycle()

    MaterialTheme {
        when (val localState = state) {
            DataState.Idle -> {}
            DataState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingIndicator()
                }
            }
            is DataState.Error -> {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = localState.msg, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
            is DataState.Success -> {
                vm.suiEngine.Render(localState.payload)
            }
        }
    }
}