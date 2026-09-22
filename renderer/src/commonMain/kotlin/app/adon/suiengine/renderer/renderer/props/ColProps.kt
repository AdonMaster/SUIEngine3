package app.adon.suiengine.renderer.renderer.props

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.toHorizontalAlignment
import app.adon.suiengine.renderer.extensions.toVerticalArrangement
import app.adon.suiengine.renderer.node.eval.evalToStr
import app.adon.suiengine.renderer.node.modifier.extractModifier

data class ColProps(
    val modifier: Modifier,
    val vArrange: Arrangement.Vertical,
    val hAlign: Alignment.Horizontal
)

fun Context.resolveColProps(): ColProps {
    return ColProps(
        modifier = node.extractModifier(this),
        vArrange = node.params.firstOrNull { it.name == "v_arrange" }?.value
            ?.evalToStr(this)
            ?.toVerticalArrangement ?: Arrangement.Top,
        hAlign = node.params.firstOrNull { it.name == "h_align" }?.value
            ?.evalToStr(this)
            ?.toHorizontalAlignment ?: Alignment.Start
    )
}