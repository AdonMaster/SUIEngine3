package app.adon.suiengine.renderer

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.extensions.register
import app.adon.suiengine.renderer.form.FormFieldSavers
import app.adon.suiengine.renderer.node.paramSolver
import app.adon.suiengine.renderer.node.resolve
import app.adon.suiengine.renderer.node.resolveValStr
import app.adon.suiengine.renderer.ui.extractModifier

val functionRegistryForm = buildMap<String, @Composable (Node.Fn, Context) -> Unit> {

    register("@form") { node: Node.Fn, context: Context ->
        val paramSolver = node.paramSolver("name", "fields")
        val nameValue = paramSolver.get("name")?.stringableVal() ?: "default_form"

        //
        var fields = Node.Dict(emptyMap())
        val fieldsParam = paramSolver.get("fields")
        if (fieldsParam != null) {
            fields = fieldsParam.resolve(context) as? Node.Dict
                ?: run {
                    context.raise("@form fields param must be a map/dictionary")
                    return@register
                }
        }

        //
        val formContext = remember(node.uid) { context.newFormChild(nameValue, fields) }
        FunctionRegistry.renderers["col"]?.invoke(node, formContext)
    }

    register("text_field") { node: Node.Fn, context: Context ->
        val paramSolver = node.paramSolver("name", "form", "label", "ph")
        var mod = extractModifier(node.params, context)

        val name = paramSolver.get("name")?.resolveValStr(context)
            ?: run {
                context.raise("textfield requer o parametro 'name'")
                return@register
            }
        val formName = paramSolver.get("form")?.resolve(context) as? Node.Str
        val formContext = context.safeClosestForm(formName?.v)
            ?: run { return@register }
        val label = paramSolver.get("label")?.resolveValStr(context)
        val ph = paramSolver.get("ph")?.resolveValStr(context)

        // remember with saver
        val textState = rememberSaveable(name, saver = FormFieldSavers.TextSaver) {
            formContext.form.getOrInitTextField(name)
        }
        // sync
        SideEffect {
            formContext.form.syncTextField(name, textState)
        }

        //
        OutlinedTextField(
            modifier = mod,
            state = textState.state,
            placeholder = ph?.let { { Text(ph) } },
            label = label?.let { { Text(label) } }
        )
    }

}