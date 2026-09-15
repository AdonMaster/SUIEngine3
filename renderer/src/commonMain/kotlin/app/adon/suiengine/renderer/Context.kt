package app.adon.suiengine.renderer

import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.extensions.upwards

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

    // state
    private val stateBinding = mutableMapOf<String, String>()
    fun storeState(stablePrefix: String, key: String?, value: Node) {
        if (key == null) {
            raise("store state should have a name")
        } else {
            val stableKey = "$stablePrefix.$key"
            vm.setState(stableKey, value)
            stateBinding[key] = stableKey
        }
    }

    fun safeStoreState(key: String?, value: Node) {
        if (key == null) {
            raise("store state should have a name")
        } else {
            val stableKey = findStableKey(key)
            if (stableKey == null) {
                raise("state [$key] not found")
            } else {
                vm.setState(stableKey, value)
            }
        }
    }

    private fun findStableKey(key: String): String? {
        return stateBinding[key] ?: parent?.findStableKey(key)
    }

    fun retrieveState(key: String): Node {
        val stableKey = findStableKey(key)
        if (stableKey != null) return vm.states[stableKey] ?: Node.Null
        // fail
        raise("state [$key] not found")
        return Node.Null
    }
}