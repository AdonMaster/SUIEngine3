package app.adon.suiengine.renderer.renderer.props

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.toHorizontalArrangement
import app.adon.suiengine.renderer.extensions.toVerticalAlignment
import app.adon.suiengine.renderer.node.eval.evalToStr
import app.adon.suiengine.renderer.node.modifier.extractModifier

data class RowProps(
    val modifier: Modifier,
    val hArrange: Arrangement.Horizontal,
    val vAlign: Alignment.Vertical,
    val children: List<Node.Fn>
)

fun Node.Fn.resolveRowProps(context: Context): RowProps {
    return RowProps(
        modifier = extractModifier(context),
        hArrange = params.firstOrNull { it.name == "h_arrange" }?.value
            ?.evalToStr(context)
            ?.toHorizontalArrangement ?: Arrangement.Start,
        vAlign = params.firstOrNull { it.name == "v_align" }?.value
            ?.evalToStr(context)
            ?.toVerticalAlignment ?: Alignment.Top,
        children = children.filterIsInstance<Node.Fn>()
    )
}