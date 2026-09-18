package app.adon.suiengine.renderer

import androidx.compose.foundation.text.input.TextFieldState
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.form.FormFieldState
import app.adon.suiengine.renderer.node.resolveValStr

class FormController(private val context: FormContext, val fields: Node.Dict) {

    private val fieldStates = mutableMapOf<String, FormFieldState>()

    fun getOrInitTextField(name: String): FormFieldState.Text {
        val existing = fieldStates[name]
        if (existing is FormFieldState.Text) {
            return existing
        }

        val initialValue = fields.v[name]?.resolveValStr(context) ?: ""
        val newState = FormFieldState.Text(TextFieldState(initialValue))
        fieldStates[name] = newState
        return newState
    }

    fun syncTextField(name: String, state: FormFieldState.Text) {
        fieldStates[name] = state
    }

}

class FormContext(name: String, parent: Context?, vm: SUIEngineVM, fields: Node.Dict) : Context(name, parent, vm) {

    val form = FormController(this, fields)

}