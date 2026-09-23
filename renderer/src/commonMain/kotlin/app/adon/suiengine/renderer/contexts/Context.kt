package app.adon.suiengine.renderer.contexts

import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.SUIEngineVM
import app.adon.suiengine.renderer.extensions.upwards


//
class StateStoreKeyNotFoundException(key: String): Exception("state store [$key] not found")

//
open class Context(val name: String, val parent: Context?, private val vm: SUIEngineVM) {

    // children factory
    fun newChild(name: String) = Context(name, this, vm)
    fun newFormChild(name: String) = FormContext(name, this, vm)

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
    open fun retrieveVirtual(key: String): Node? {
        return virtualState[key] ?: parent?.retrieveVirtual(key)
    }

    // state
    private val stateBinding = mutableMapOf<String, String>()
    fun getOrPutState(stableId: String, key: String, value: Node): Node {
        val bindingKey = "${stableId}.${key}"
        stateBinding[key] = bindingKey
        return vm.stateGetOrPut(bindingKey, value)
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
        vm.statePut(bindingKey, value)
    }

}

