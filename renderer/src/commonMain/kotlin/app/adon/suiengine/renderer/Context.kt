package app.adon.suiengine.renderer

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.extensions.upwards
import app.adon.suiengine.renderer.ui.LayoutScope

class Context(
    val name: String, val parent: Context?, val vm: SUIEngineVM
) {

    // error
    fun raise(reason: String) {
        val stack = mutableListOf<String>("error: $reason")
        upwards { stack.add("from: ${it.name}") }
        vm.setErrors(stack)
    }

    // clone
    fun newChild(name: String): Context {
        return Context(
            name = name,
            parent = this,
            vm = vm
        )
    }

    // layout scope
    private var _layoutScope: LayoutScope? = null
    val layoutScope get() = _layoutScope
    fun setLayoutScope(scope: LayoutScope?): Context {
        _layoutScope = scope
        return this
    }
    fun withLayoutScope(scope: ColumnScope) = setLayoutScope(LayoutScope.Col(scope))
    fun withLayoutScope(scope: RowScope) = setLayoutScope(LayoutScope.Row(scope))
    fun withLayoutScope(scope: BoxScope) = setLayoutScope(LayoutScope.Box(scope))

    // state
    private val stateBinding = mutableMapOf<String, String>()
    fun initialState(stablePrefix: String, key: String?, value: Node) {
        if (key == null) {
            raise("store state should have a name")
        } else {
            val stableKey = "$stablePrefix.$key"
            vm.setState(stableKey, value)
            stateBinding[key] = stableKey
        }
    }

    fun safeStoreState(key: String?, value: Node) {
        runCatching {
            unsafeStoreState(key, value)
        }.onFailure {
            raise(it.message!!)
        }
    }

    fun unsafeStoreState(key: String?, value: Node) {
        if (key == null) throw Exception("store state should have a name")
        val stableKey = findStableKey(key) ?: throw Exception("state [$key] not found")
        vm.setState(stableKey, value)
    }

    private fun findStableKey(key: String): String? {
        return stateBinding[key] ?: parent?.findStableKey(key)
    }

    fun retrieveState(path: List<String>): Node
    {
        if (path.isEmpty()) return Node.Null
        val rootKey = path.first()

        // search for virtual first
        retrieveVirtualState(rootKey)?.let { return it }

        // then it checks viewmodel state
        val stableKey = findStableKey(rootKey)
        if (stableKey != null) return vm.states[stableKey] ?: Node.Null
        // fail
        raise("state [$rootKey] not found")
        return Node.Null
    }


    // virtual state
    private val virtualStore = mutableMapOf<String, Node>()
    fun setVirtual(key: String, value: Node) {
        virtualStore[key] = value
    }

    private fun retrieveVirtualState(key: String): Node? {
        return virtualStore[key] ?: parent?.retrieveVirtualState(key)
    }
}