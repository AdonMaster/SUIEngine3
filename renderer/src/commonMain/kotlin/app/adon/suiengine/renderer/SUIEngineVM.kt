package app.adon.suiengine.renderer

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.adon.suiengine.ast.Lexer
import app.adon.suiengine.ast.Node
import app.adon.suiengine.ast.Parser
import app.adon.suiengine.ast.normalizeIfChains
import app.adon.suiengine.renderer.state.DataState
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SUIEngineVM(
    private val rawCode: String
) : ViewModel() {

    //
    private val _nodes = MutableStateFlow<DataState<List<Node>>>(DataState.Idle)
    val nodes = _nodes.asStateFlow()

    //
    init {
        viewModelScope.launch {
            setup()
        }
    }

    // setup
    private suspend fun setup() {
        _nodes.value = DataState.Loading
        withContext(NonCancellable) {
            _nodes.value = try {
                val lexer = Lexer(rawCode)
                val parser = Parser(lexer.tokenize())
                val nodes = parser.parse()
                    .normalizeIfChains()
                DataState.Success(nodes)
            } catch (e: Exception) {
                DataState.Error(e.message ?: "err:122")
            }
        }
    }

    // err
    private val _errStack = MutableStateFlow<List<List<String>>>(emptyList())
    val errStack = _errStack.asStateFlow()
    fun clearErrStack() { _errStack.value = emptyList() }
    fun raise(list: List<String>) { _errStack.update { cur -> cur + listOf(list) } }

    // state
    private val _states = mutableStateMapOf<String, Node>()
    val states: SnapshotStateMap<String, Node> = _states
    fun stateGetOrPut(key: String, value: Node): Node {
        return states.getOrPut(key) {
            value
        }
    }
    fun statePut(key: String, value: Node) {
        states[key] = value
    }

}