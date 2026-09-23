package app.adon.suiengine.renderer.renderer.props

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.toAlignment
import app.adon.suiengine.renderer.node.eval.evalToStr
import app.adon.suiengine.renderer.node.modifier.extractModifier

data class BoxProps(
    val modifier: Modifier,
    val align: Alignment,
    val children: List<Node.Fn>
)

fun Node.Fn.resolveBoxProps(context: Context): BoxProps {
    return BoxProps(
        modifier = extractModifier(context),
        align = params
            .firstOrNull { it.name == "align" }?.value
            ?.evalToStr(context)
            ?.toAlignment ?: Alignment.Center,
        children = children.filterIsInstance<Node.Fn>()
    )
}