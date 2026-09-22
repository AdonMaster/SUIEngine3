package app.adon.suiengine.renderer.renderer.props

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.toHorizontalArrangement
import app.adon.suiengine.renderer.extensions.toVerticalAlignment
import app.adon.suiengine.renderer.node.eval.evalToStr
import app.adon.suiengine.renderer.node.modifier.extractModifier

data class RowProps(
    val modifier: Modifier,
    val hArrange: Arrangement.Horizontal,
    val vAlign: Alignment.Vertical
)

fun Context.resolveRowProps(): RowProps {
    return RowProps(
        modifier = node.extractModifier(this),
        hArrange = node.params.firstOrNull { it.name == "h_arrange" }?.value
            ?.evalToStr(this)
            ?.toHorizontalArrangement ?: Arrangement.Start,
        vAlign = node.params.firstOrNull { it.name == "v_align" }?.value
            ?.evalToStr(this)
            ?.toVerticalAlignment ?: Alignment.Top
    )
}