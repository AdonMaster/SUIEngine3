package app.adon.suiengine.renderer

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.adon.suiengine.ast.Lexer
import app.adon.suiengine.ast.Node
import app.adon.suiengine.ast.Parser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    // Dynamic Node States
    private val _states = mutableStateMapOf<String, Node>()
    val states: SnapshotStateMap<String, Node> = _states

    fun setState(key: String, value: Node) {
        _states[key] = value
    }

    // AST Nodes & Parse Control
    private val _nodes = MutableStateFlow<List<Node>>(emptyList())
    val nodes: StateFlow<List<Node>> = _nodes.asStateFlow()

    private val _parseError = MutableStateFlow<String?>(null)
    val parseError: StateFlow<String?> = _parseError.asStateFlow()

    private var lastParsedPayload: String? = null

    fun parseIfNeeded(payload: String) {
        //
        if (lastParsedPayload == payload && _nodes.value.isNotEmpty()) return
        lastParsedPayload = payload

        viewModelScope.launch(Dispatchers.Default) {
            runCatching {
                val lexer = Lexer(payload)
                val parser = Parser(lexer.tokenize())
                parser.parse()
            }.onSuccess { parsedNodes ->
                _nodes.value = parsedNodes
                _parseError.value = null
            }.onFailure {
                _nodes.value = emptyList()
                _parseError.value = it.message ?: "Erro desconhecido ao processar AST"
            }
        }
    }
}