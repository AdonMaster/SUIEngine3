package app.adon.suiengine.renderer.renderer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.register
import app.adon.suiengine.renderer.extensions.registerComponent
import app.adon.suiengine.renderer.extensions.toTextStyle
import app.adon.suiengine.renderer.invoker.Invoker
import app.adon.suiengine.renderer.layout.LayoutScope
import app.adon.suiengine.renderer.layout.toLayoutScope
import app.adon.suiengine.renderer.node.modifier.extractModifier
import app.adon.suiengine.renderer.node.paramSolver
import app.adon.suiengine.renderer.renderer.props.SurfaceProps
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
        Box(
            modifier = p.modifier,
            contentAlignment = p.contentAlignment ?: Alignment.TopStart
        ) {
            val childContext = context.newChild("box", this.toLayoutScope())
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
            val childContext = context.newChild("col", this.toLayoutScope())
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
            val childContext = context.newChild("row", this.toLayoutScope())
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
        val style = p.styleKey?.toTextStyle() ?: LocalTextStyle.current
        Text(
            text = p.text,
            modifier = p.modifier, fontStyle = p.fontStyle, textAlign = p.textAlign,
            color = p.color, fontSize = p.fontSize, lineHeight = p.lineHeight, overflow = p.overflow,
            fontWeight = p.weight, style = style
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
            val childContext = context.newChild("btn", this.toLayoutScope())
            RenderGroup(p.children, childContext)
        }
    }

    registerComponent(
        "surface",
        resolveProps = { node, context -> SurfaceProps.from(node, context) }
    ) { p, context ->

        val shape = RoundedCornerShape(p.cornerRadius.dp)
        val bg = p.backgroundColor ?: MaterialTheme.colorScheme.surface
        val fg = p.foregroundColor ?: MaterialTheme.colorScheme.onSurface

        // ugly... but I believe is the only way to make it clean
        if (p.onTouch != null) {
            Surface(
                modifier = p.modifier,
                onClick = {
                    Invoker.trigger(p.onTouch, context)
                },
                shape = shape,
                color = bg,
                contentColor = fg,
                shadowElevation = p.elevation.dp
            ) {
                val childContext = context.newChild("surface", LayoutScope.None)
                RenderGroup(p.children, childContext)
            }
        } else {
            Surface(
                modifier = p.modifier,
                shape = shape,
                color = bg,
                contentColor = fg,
                shadowElevation = p.elevation.dp
            ) {
                val childContext = context.newChild("surface", LayoutScope.None)
                RenderGroup(p.children, childContext)
            }
        }
    }

    register("dialog") { fn, context ->
        val paramSolver = fn.paramSolver("on_dismiss")
        val onDismiss = paramSolver.get("on_dismiss")
        val hasDismissAction = onDismiss != null
        Dialog(
            onDismissRequest = {
                Invoker.trigger(onDismiss, context)
            },
            properties = DialogProperties(
                dismissOnBackPress = hasDismissAction,
                dismissOnClickOutside = hasDismissAction,
                usePlatformDefaultWidth = false
            )
        ) {
            val childContext = context.newChild("dialog", LayoutScope.None)
            RenderGroup(fn.children, childContext)
        }
    }

}
