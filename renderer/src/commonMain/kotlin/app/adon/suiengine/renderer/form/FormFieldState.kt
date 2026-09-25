package app.adon.suiengine.renderer.form

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.adon.suiengine.ast.Node
import app.adon.suiengine.ast.toNode

sealed class FormFieldState {

    class Text(val state: TextFieldState) : FormFieldState() {
        val value: String get() = state.text.toString()
    }

    class Bool(initial: Boolean) : FormFieldState() {
        var value by mutableStateOf(initial)
    }

    fun toNode(): Node = when(this) {
        is Bool -> value.toNode()
        is Text -> value.toNode()
    }

}