package app.adon.suiengine.renderer

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText

class FormContext(name: String, parent: Context?, vm: SUIEngineVM) : Context(name, parent, vm) {

    private val textFields = mutableMapOf<String, TextFieldState>()

    fun addField(name: String, state: TextFieldState) {
        textFields[name] = state
    }

    fun collectValues(): Map<String, String> {
        return textFields.mapValues { it.value.text.toString() }
    }

    fun resetForm() {
        textFields.values.forEach { state ->
            state.clearText()
        }
    }

}