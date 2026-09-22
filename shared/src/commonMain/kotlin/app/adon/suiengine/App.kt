package app.adon.suiengine

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.adon.suiengine.renderer.SUIEngine
import app.adon.suiengine.renderer.components.TextError
import app.adon.suiengine.renderer.playground.SuiEnginePlayground
import app.adon.suiengine.renderer.resource.Resource
import app.adon.suiengine.renderer.state.DataState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun App(
    vm: AppVM = viewModel { AppVM() }
) {

    val exampleState by vm.example.state

    Box(
        modifier = Modifier
            .fillMaxSize()
        ,
        contentAlignment = Alignment.Center
    ) {
        when (val current = exampleState) {
            DataState.Idle -> Text("Idle")
            DataState.Loading -> CircularProgressIndicator()
            is DataState.Success -> SUIEngine(current.payload)
            is DataState.Error -> TextError(current.reason, modifier = Modifier.fillMaxWidth().padding(20.dp))
        }
    }

}