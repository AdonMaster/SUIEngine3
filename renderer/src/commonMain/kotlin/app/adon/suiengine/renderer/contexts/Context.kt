package app.adon.suiengine.renderer.contexts

import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.SUIEngineVM
import app.adon.suiengine.renderer.extensions.upwards


//
class StateStoreKeyNotFoundException(key: String): Exception("state store [$key] not found")

//
open class Context(val name: String, val parent: Context?, private val vm: SUIEngineVM) {

    // children
    private val _children = mutableListOf<Context>()
    val children: List<Context> get() = _children
    fun newChild(name: String) = Context(name, this, vm)
        .also { _children.add(it) }
    fun newIfElseChild(type: IfElseType, resolved: Boolean) = IfElseContext(type, resolved, this, vm)
        .also { _children.add(it) }


    // raise
    fun raise(reason: String) {
        val stack = mutableListOf(reason)
        upwards { stack.add(0, it.name); true }
        vm.raise(stack)
    }
    fun raise(throwable: Throwable) = raise(throwable.message ?: "err:112")

    // virtual state
    private val virtualState = mutableMapOf<String, Node>()
    fun setVirtual(key: String, value: Node) {
        virtualState[key] = value
    }
    private fun retrieveVirtual(key: String): Node? {
        return virtualState[key] ?: parent?.retrieveVirtual(key)
    }

    // state
    private val stateBinding = mutableMapOf<String, String>()
    fun initState(stableId: String, key: String, value: Node) {
        val bindingKey = "${stableId}.${key}"
        stateBinding[key] = bindingKey
        vm.initState(bindingKey, value)
    }
    private fun findBindingKey(key: String): String? {
        return stateBinding[key] ?: parent?.findBindingKey(key)
    }
    fun getState(key: String): Node {
        return retrieveVirtual(key) ?: run {
            val bindingKey = findBindingKey(key) ?: throw Exception("variavel [$key] nao encontrada")
            vm.states[bindingKey]
        } ?: throw StateStoreKeyNotFoundException(key)
    }
    fun setState(key: String, value: Node) {
        val bindingKey = findBindingKey(key) ?: throw StateStoreKeyNotFoundException(key)
        vm.initState(bindingKey, value)
    }

}
