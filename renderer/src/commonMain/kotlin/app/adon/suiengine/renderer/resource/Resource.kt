package app.adon.suiengine.renderer.resource

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import app.adon.suiengine.renderer.state.DataState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class Resource<T>(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
    private val cb: suspend ()->T
) {

    private val _state = mutableStateOf<DataState<T>>(DataState.Idle)
    val state: State<DataState<T>> = _state

    init {
        resolve()
    }

    fun resolve() {
        _state.value = DataState.Loading
        scope.launch {
            try {
                _state.value = DataState.Success(cb())
            } catch (e: Exception) {
                _state.value = DataState.Error(e.message ?: "resource: 14323")
            }
        }
    }

}