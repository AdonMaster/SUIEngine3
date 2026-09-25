package app.adon.suiengine.renderer.renderer.props

import androidx.compose.ui.Modifier
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.findSelfAndAncestors
import app.adon.suiengine.renderer.form.FormFieldState
import app.adon.suiengine.renderer.node.eval.eval
import app.adon.suiengine.renderer.node.modifier.extractModifier
import app.adon.suiengine.renderer.node.paramSolver
import app.adon.suiengine.renderer.utils.takeAs

data class InputProps(
    val fieldName: String,
    val initialValue: String,
    val targetContext: Context,
    val state: FormFieldState.Text,
    val label: String?,
    val modifier: Modifier,
    val rememberKey: String
)

fun Node.Fn.resolveInputProps(context: Context): InputProps {
    val ps = paramSolver("name", "form", "value", "label")

    //
    val fieldName = ps.get("name")?.eval(context)?.takeAs<Node.Str>()?.v
        ?: "input_${uid}"

    //
    val formName = ps.get("form")?.eval(context)?.takeAs<Node.Str>()?.v
    val targetContext = context.findSelfAndAncestors<Context.Form> {
        formName == null || it.name == formName
    } ?: context

    //
    val initialValue = ps.get("value")?.stringableVal() ?: ""
    val label = ps.get("label")?.stringableVal()

    // init state
    val state = targetContext.getOrPutFieldState(fieldName, initialValue)

    //
    return InputProps(
        fieldName = fieldName,
        initialValue = initialValue,
        targetContext = targetContext,
        state = state,
        label = label,
        modifier = extractModifier(context),
        rememberKey = "${fieldName}_${uid}"
    )
}
