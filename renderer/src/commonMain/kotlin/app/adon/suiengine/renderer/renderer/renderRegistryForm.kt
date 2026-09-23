package app.adon.suiengine.renderer.renderer

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.contexts.FormContext
import app.adon.suiengine.renderer.extensions.findSelfAndAncestors
import app.adon.suiengine.renderer.extensions.registerComponent
import app.adon.suiengine.renderer.node.eval.eval
import app.adon.suiengine.renderer.node.modifier.extractModifier
import app.adon.suiengine.renderer.node.paramSolver
import app.adon.suiengine.renderer.renderer.props.resolveColProps
import app.adon.suiengine.renderer.utils.takeAs


val renderRegistryForm = buildMap<String, @Composable (Node.Fn, Context) -> Unit> {

    registerComponent(
        "form",
        resolveProps = { node, context ->
            val name = node.params.firstOrNull()?.value?.eval(context)?.takeAs<Node.Str>()?.v
                ?: "default"
            val props = node.resolveColProps(context)
            Triple(node, name, props)
        }
    ) { props, context ->
        val (fn, name, props) = props

        Column(modifier = props.modifier) {
            val formContext = context.newFormChild(name)
            RenderGroup(fn.children, formContext)
        }
    }

    registerComponent(
        "input",
        resolveProps = { node, context ->
            val ps = node.paramSolver("name", "form")
            val fieldName = ps.get("name")?.eval(context)?.takeAs<Node.Str>()?.v
                ?: throw RuntimeException("[input] deve ter param name")
            val formName = ps.get("form")?.eval(context)?.takeAs<Node.Str>()?.v
            val modifier = node.extractModifier(context)
            Triple(formName, fieldName, modifier)
        }
    ) { props, context ->

        val (formName, fieldName, modifier) = props

        //
        val formContext = context.findSelfAndAncestors<FormContext> { formName == null || it.name == formName }
            ?: error("O componente [input] ('$fieldName') deve estar dentro de um [form]")

        //
        val fieldState = formContext.getOrPutFieldState(
            fieldName = fieldName,
            initialValue = ""
        )

        OutlinedTextField(
            state = fieldState,
            label = { Text(fieldName) },
            modifier = modifier
        )
    }

}