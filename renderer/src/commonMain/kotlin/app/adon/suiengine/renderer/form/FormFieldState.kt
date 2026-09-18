package app.adon.suiengine.renderer.form

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

sealed class FormFieldState {

    class Text(val state: TextFieldState) : FormFieldState() {
        val value: String get() = state.text.toString()
    }

    class Bool(initial: Boolean) : FormFieldState() {
        var value by mutableStateOf(initial)
    }

    class Number(initial: Double) : FormFieldState() {
        var value by mutableStateOf(initial)
    }

}