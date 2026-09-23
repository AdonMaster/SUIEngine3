package app.adon.suiengine.renderer.renderer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.registerComponent
import app.adon.suiengine.renderer.invoker.Invoker
import app.adon.suiengine.renderer.node.modifier.extractModifier
import app.adon.suiengine.renderer.renderer.props.resolveBoxProps
import app.adon.suiengine.renderer.renderer.props.resolveButtonProps
import app.adon.suiengine.renderer.renderer.props.resolveColProps
import app.adon.suiengine.renderer.renderer.props.resolveRowProps
import app.adon.suiengine.renderer.renderer.props.resolveTextProps


val renderRegistryMisc = buildMap<String, @Composable (Node.Fn, Context) -> Unit> {

    registerComponent(
        "box",
        resolveProps = { node, context -> node.resolveBoxProps(context) }
    ) { p, context ->
        Box(modifier = p.modifier, contentAlignment = p.align) {
            val childContext = context.newChild("box")
            RenderGroup(p.children, childContext)
        }
    }

    registerComponent(
        "col",
        resolveProps = { node, context -> node.resolveColProps(context) }
    ) { p, context ->
        Column(
            modifier = p.modifier,
            verticalArrangement = p.vArrange,
            horizontalAlignment = p.hAlign
        ) {
            val childContext = context.newChild("col")
            RenderGroup(p.children, childContext)
        }
    }

    registerComponent(
        "row",
        resolveProps = { node, context -> node.resolveRowProps(context) }
    ) { p, context ->
        Row(
            modifier = p.modifier,
            horizontalArrangement = p.hArrange,
            verticalAlignment = p.vAlign
        ) {
            val childContext = context.newChild("row")
            RenderGroup(p.children, childContext)
        }
    }

    registerComponent(
        "spacer",
        resolveProps = { node, context -> node.extractModifier(context) }
    ) { p, _ ->
        Spacer(modifier = p)
    }

    registerComponent(
        "divider",
        resolveProps = { node, context -> node.extractModifier(context) }
    ) { p, _ ->
        HorizontalDivider(modifier = p)
    }

    registerComponent(
        "text",
        resolveProps = { node, context -> node.resolveTextProps(context) }
    ) { p, _ ->
        Text(
            text = p.text,
            modifier = p.modifier
        )
    }

    registerComponent(
        "btn",
        resolveProps = { node, context -> node.resolveButtonProps(context) }
    ) { p, context ->
        Button(
            modifier = p.modifier,
            onClick = {
                Invoker.trigger(p.onTouch, context)
            },
        ) {
            p.text?.let { Text(it) }
            val childContext = context.newChild("text")
            RenderGroup(p.children, childContext)
        }
    }
}
