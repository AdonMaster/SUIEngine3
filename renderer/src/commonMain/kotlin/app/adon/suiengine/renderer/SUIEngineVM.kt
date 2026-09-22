package app.adon.suiengine.renderer

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.adon.suiengine.ast.Lexer
import app.adon.suiengine.ast.Node
import app.adon.suiengine.ast.Parser
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.state.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SUIEngineVM(
    private val rawCode: String
) : ViewModel() {

    //
    private val _nodes = MutableStateFlow<DataState<List<Context>>>(DataState.Idle)
    val nodes = _nodes.asStateFlow()

    //
    init {
        viewModelScope.launch {
            setup()
        }
    }

    // setup
    private fun setup() {
        with(Dispatchers.Default) {
            _nodes.value = DataState.Loading
            runCatching {
                val lexer = Lexer(rawCode)
                val parser = Parser(lexer.tokenize())
                parser.parse()
            }.onSuccess { res ->
                val nodeRoot = Node.Fn("root", emptyList(), emptyList(), null)
                val rootContext = Context(nodeRoot, null, emptyList(), this@SUIEngineVM)
                val contexts = res.filterIsInstance<Node.Fn>().map {
                    buildContextTree(it, rootContext)
                }
                _nodes.value = DataState.Success(contexts)
            }.onFailure {
                _nodes.value = DataState.Error(it.message ?: "err:122")
            }
        }
    }
    private fun buildContextTree(node: Node.Fn, parent: Context): Context {
        val currentContext = Context(
            node = node, parent = parent, children = emptyList(), vm = this@SUIEngineVM
        )
        val children = node.children.filterIsInstance<Node.Fn>().map { childNode ->
            buildContextTree(childNode, parent = currentContext)
        }
        return currentContext.copy(children = children)
    }

    // err
    private val _errStack = MutableStateFlow<List<List<String>>>(emptyList())
    val errStack = _errStack.asStateFlow()
    fun clearErrStack() { _errStack.value = emptyList() }
    fun raise(list: List<String>) { _errStack.update { cur -> cur + listOf(list) } }

    // state
    private val _states = mutableStateMapOf<String, Node>()
    val states: SnapshotStateMap<String, Node> = _states
    fun initState(key: String, value: Node) {
        states[key] = value
    }

}