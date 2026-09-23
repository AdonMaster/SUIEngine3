package app.adon.suiengine.renderer.contexts

import androidx.compose.foundation.text.input.TextFieldState
import app.adon.suiengine.ast.Node
import app.adon.suiengine.ast.toNode
import app.adon.suiengine.renderer.SUIEngineVM
import app.adon.suiengine.renderer.extensions.findSelfAndAncestors

class FormContext(formName: String, parent: Context?, vm: SUIEngineVM)
    : Context("form_$formName", parent, vm)
{
    private val fieldStates = mutableMapOf<String, TextFieldState>()
    private val fieldStatesNode get() = Node.Dict(fieldStates.entries.associate { entry ->
        entry.key to entry.value.text.toString().toNode()
    }, extension = null)

    fun getOrPutFieldState(fieldName: String, initialValue: String): TextFieldState {
        return fieldStates.getOrPut(fieldName) {
            TextFieldState(initialValue)
        }
    }

    override fun retrieveVirtual(key: String): Node? {
        val fieldValue = fieldStates[key]?.text?.toString()?.toNode()
            ?: findSelfAndAncestors<FormContext> { key == "form" || it.name == key }
                ?.fieldStatesNode
        return fieldValue ?: super.retrieveVirtual(key)
    }
}
