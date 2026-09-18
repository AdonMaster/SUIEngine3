package app.adon.suiengine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.adon.suiengine.data.DataState
import app.adon.suiengine.renderer.SUIEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import suiengine.shared.generated.resources.Res

class AppVM: ViewModel() {

    val suiEngine = SUIEngine()

    private val _state = MutableStateFlow<DataState<String>>(DataState.Idle)
    val state: StateFlow<DataState<String>> = _state.asStateFlow()

    init {
        loadScript()
    }

    private fun loadScript() {
        viewModelScope.launch {
            _state.value = DataState.Loading
            runCatching {
                val path = "files/example01.js"
                val bytes = Res.readBytes(path)
                bytes.decodeToString()
            }.onSuccess { payload ->
                _state.value = DataState.Success(payload)
            }.onFailure {
                _state.value = DataState.Error(it.message ?: "Erro desconhecido ao carregar o script")
            }
        }
    }

}