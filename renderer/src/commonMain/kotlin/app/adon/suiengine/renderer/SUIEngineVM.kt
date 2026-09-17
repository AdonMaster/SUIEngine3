package app.adon.suiengine.renderer

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import app.adon.suiengine.ast.Node
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SUIEngineVM: ViewModel() {

    // errors
    private val _errors = MutableStateFlow(emptyList<String>())
    val errors = _errors.asStateFlow()
    fun setErrors(list: List<String>) {
        if (list.isEmpty()) {
            _errors.value = emptyList()
        } else {
            _errors.value = list + _errors.value
        }
    }

    private val _states = mutableStateMapOf<String, Node>()
    val states: SnapshotStateMap<String, Node> = _states
    fun setState(key: String, value: Node) {
        _states[key] = value
    }

}