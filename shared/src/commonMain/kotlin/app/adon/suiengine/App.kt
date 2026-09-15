package app.adon.suiengine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import app.adon.suiengine.data.DataState
import app.adon.suiengine.renderer.SUIEngine
import suiengine.shared.generated.resources.Res

@Composable
@Preview
fun App() {

    val suiengine = remember { SUIEngine() }
    var state by remember { mutableStateOf<DataState<String>>(DataState.Idle) }

    //
    LaunchedEffect(Unit) {
        if (state is DataState.Idle) {
            state = DataState.Loading
            runCatching {
                val path = "files/example01.js"
                val bytes = Res.readBytes(path)
                state = DataState.Success(bytes.decodeToString())
            }.onFailure {
                state = DataState.Error(it.message ?: "Erro desconhecido ao carregar o script")
            }
        }
    }

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
                        .padding(24.dp)
                    ,
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = localState.msg, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
            is DataState.Success<*> -> {
                suiengine.Render()
            }
        }
    }
}