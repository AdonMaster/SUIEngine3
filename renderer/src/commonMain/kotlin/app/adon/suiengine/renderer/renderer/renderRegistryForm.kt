package app.adon.suiengine.renderer.renderer

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.registerComponent
import app.adon.suiengine.renderer.form.FormFieldSavers
import app.adon.suiengine.renderer.renderer.props.resolveFormProps
import app.adon.suiengine.renderer.renderer.props.resolveInputProps


val renderRegistryForm = buildMap<String, @Composable (Node.Fn, Context) -> Unit> {

    registerComponent(
        "form",
        resolveProps = { node, context -> node.resolveFormProps(context) }
    ) { props, context ->
        Column(
            modifier = props.colProps.modifier,
            verticalArrangement = props.colProps.vArrange,
            horizontalAlignment = props.colProps.hAlign
        ) {
            val formContext = remember(props.rememberKey) { context.newFormChild(props.formName, layoutScope = this) }
            RenderGroup(props.children, formContext)
        }
    }

    registerComponent(
        "input",
        resolveProps = { node, context -> node.resolveInputProps(context) }
    ) { props, _ ->

        // saveable
        val stateRecovered = rememberSaveable(props.rememberKey, saver = FormFieldSavers.TextSaver) {
            props.state
        }

        // content synchronization
        SideEffect {
            props.targetContext.syncField(props.fieldName, stateRecovered.value)
        }

        OutlinedTextField(
            state = props.state.state,
            label = props.label?.let { { Text(it) } },
            modifier = props.modifier
        )
    }

}