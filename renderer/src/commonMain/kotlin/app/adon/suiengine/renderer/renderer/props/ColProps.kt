package app.adon.suiengine.renderer.renderer.props

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.toHorizontalAlignment
import app.adon.suiengine.renderer.extensions.toVerticalArrangement
import app.adon.suiengine.renderer.node.eval.evalToStr
import app.adon.suiengine.renderer.node.modifier.extractModifier

data class ColProps(
    val modifier: Modifier,
    val vArrange: Arrangement.Vertical,
    val hAlign: Alignment.Horizontal,
    val children: List<Node.Fn>
)

fun Node.Fn.resolveColProps(context: Context): ColProps {
    return ColProps(
        modifier = extractModifier(context),
        vArrange = params.firstOrNull { it.name == "v_arrange" }?.value
            ?.evalToStr(context)
            ?.toVerticalArrangement ?: Arrangement.Top,
        hAlign = params.firstOrNull { it.name == "h_align" }?.value
            ?.evalToStr(context)
            ?.toHorizontalAlignment ?: Alignment.Start,
        children = children.filterIsInstance<Node.Fn>()
    )
}