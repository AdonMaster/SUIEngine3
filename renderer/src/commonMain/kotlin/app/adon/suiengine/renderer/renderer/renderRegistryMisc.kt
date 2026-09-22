package app.adon.suiengine.renderer.renderer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.registerComponent
import app.adon.suiengine.renderer.invoker.Invoker
import app.adon.suiengine.renderer.node.modifier.extractModifier
import app.adon.suiengine.renderer.renderer.props.resolveBoxProps
import app.adon.suiengine.renderer.renderer.props.resolveButtonProps
import app.adon.suiengine.renderer.renderer.props.resolveColProps
import app.adon.suiengine.renderer.renderer.props.resolveRowProps
import app.adon.suiengine.renderer.renderer.props.resolveTextProps


val renderRegistryMisc = buildMap<String, @Composable (Context) -> Unit> {

    registerComponent(
        "box",
        resolveProps = { resolveBoxProps() }
    ) { p ->
        Box(modifier = p.modifier, contentAlignment = p.align) {
            RenderGroup(children)
        }
    }

    registerComponent(
        "col",
        resolveProps = { resolveColProps() }
    ) { props ->
        Column(
            modifier = props.modifier,
            verticalArrangement = props.vArrange,
            horizontalAlignment = props.hAlign
        ) {
            RenderGroup(children)
        }
    }

    registerComponent(
        "row",
        resolveProps = { resolveRowProps() }
    ) { props ->
        Row(
            modifier = props.modifier,
            horizontalArrangement = props.hArrange,
            verticalAlignment = props.vAlign
        ) {
            RenderGroup(children)
        }
    }

    registerComponent(
        "spacer",
        resolveProps = { node.extractModifier(this) }
    ) { mod ->
        Spacer(modifier = mod)
    }

    registerComponent(
        "text",
        resolveProps = { resolveTextProps() }
    ) { props ->
        Text(
            text = props.text,
            modifier = props.modifier
        )
    }

    registerComponent(
        "btn",
        resolveProps = { resolveButtonProps() }
    ) { props ->
        Button(
            modifier = props.modifier,
            onClick = {
                Invoker.trigger(props.onTouch, this)
            },
        ) {
            props.text?.let { Text(it) }
            RenderGroup(children)
        }
    }
}
