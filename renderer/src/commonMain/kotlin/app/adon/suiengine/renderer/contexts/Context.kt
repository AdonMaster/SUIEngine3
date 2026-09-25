package app.adon.suiengine.renderer.contexts

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.text.input.setTextAndSelectAll
import app.adon.suiengine.ast.Node
import app.adon.suiengine.ast.toNode
import app.adon.suiengine.renderer.SUIEngineVM
import app.adon.suiengine.renderer.extensions.findSelfAndAncestors
import app.adon.suiengine.renderer.extensions.upwards
import app.adon.suiengine.renderer.form.FormFieldState
import app.adon.suiengine.renderer.layout.LayoutScope
import app.adon.suiengine.renderer.utils.coalesce


//
class StateStoreKeyNotFoundException(key: String): Exception("state store [$key] not found")

//
sealed class Context(val name: String, val layoutScope: LayoutScope, val parent: Context?, private val vm: SUIEngineVM) {

    class Root(name: String, layoutScope: LayoutScope, vm: SUIEngineVM)
        : Context(name, layoutScope, null, vm)
    class Default(name: String, layoutScope: LayoutScope, parent: Context?, vm: SUIEngineVM)
        : Context(name, layoutScope, parent, vm)
    class Form(formName: String, layoutScope: LayoutScope, parent: Context?, vm: SUIEngineVM):
        Context(formName, layoutScope, parent, vm)

    // children factory
    fun newChild(name: String, layoutScope: LayoutScope) =
        Default(name, layoutScope, this, vm)
    fun newFormChild(name: String, layoutScope: ColumnScope) =
        Form(name, LayoutScope.Col(layoutScope), this, vm)


    // raise
    fun raise(reason: String) {
        val stack = mutableListOf(reason)
        upwards { stack.add(0, it.name); true }
        vm.raise(stack)
    }
    fun raise(throwable: Throwable) = raise(throwable.message ?: "err:112")

    // field state
    private val fieldStates = mutableMapOf<String, FormFieldState>()
    val fieldStatesNode get() = Node.Dict(fieldStates.entries.associate { entry ->
        entry.key to entry.value.toNode()
    }, extension = null)
    fun getOrPutFieldState(fieldName: String, initialValue: String): FormFieldState.Text {
        val vv = fieldStates[fieldName]
        return coalesce({
            vv as? FormFieldState.Text
        }, def = {
            FormFieldState.Text(TextFieldState(initialValue))
                .also { fieldStates[fieldName] = it }
        })
    }
    private fun retrieveField(key: String): FormFieldState? {
        return fieldStates[key] ?: parent?.retrieveField(key)
    }
    fun syncField(fieldName: String, recovered: String) {
        (fieldStates[fieldName] as? FormFieldState.Text)?.state
            ?.edit { replace(0, length, recovered) }
    }

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
    fun getOrPutState(stableId: String, key: String, value: Node): Node {
        val bindingKey = "${stableId}.${key}"
        stateBinding[key] = bindingKey
        return vm.stateGetOrPut(bindingKey, value)
    }
    private fun findBindingKey(key: String): String? {
        return stateBinding[key] ?: parent?.findBindingKey(key)
    }
    fun getState(key: String): Node {
        // 1. virtual, 2. form object, 3. local fields, 4. vm state
        return coalesce({
            retrieveVirtual(key)
        }, {
            findSelfAndAncestors<Form> { ff -> key == "form" || ff.name == key }
                ?.fieldStatesNode
        }, {
            retrieveField(key)?.toNode()
        }, {
            val bindingKey = findBindingKey(key) ?: throw Exception("variavel [$key] nao encontrada")
            vm.states[bindingKey]
        }) ?: throw StateStoreKeyNotFoundException(key)
    }
    fun setState(key: String, value: Node) {
        val bindingKey = findBindingKey(key) ?: throw StateStoreKeyNotFoundException(key)
        vm.statePut(bindingKey, value)
    }

}

