package app.adon.suiengine.renderer.form

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.saveable.Saver

object FormFieldSavers {

    val TextSaver = Saver<FormFieldState.Text, String>(
        save = { it.value },
        restore = { FormFieldState.Text(TextFieldState(it)) }
    )

    val BoolSaver = Saver<FormFieldState.Bool, Boolean>(
        save = { it.value },
        restore = { FormFieldState.Bool(it) }
    )
}