package app.adon.suiengine.renderer.form

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.saveable.Saver
import app.adon.suiengine.ast.Node

object FormFieldSavers {

    val TextSaver = Saver<FormFieldState.Text, String>(
        save = { it.value },
        restore = { FormFieldState.Text(TextFieldState(it)) }
    )

    val BoolSaver = Saver<FormFieldState.Bool, Boolean>(
        save = { it.value },
        restore = { FormFieldState.Bool(it) }
    )

    val NumberSaver = Saver<FormFieldState.Number, Double>(
        save = { it.value },
        restore = { FormFieldState.Number(it) }
    )

}