package app.adon.suiengine.renderer.renderer.props

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.toAlignment
import app.adon.suiengine.renderer.node.eval.evalToStr
import app.adon.suiengine.renderer.node.modifier.extractModifier

data class BoxProps(
    val modifier: Modifier,
    val align: Alignment
)

fun Context.resolveBoxProps(): BoxProps {
    return BoxProps(
        modifier = node.extractModifier(this),
        align = node.params
            .firstOrNull { it.name == "align" }?.value
            ?.evalToStr(this)
            ?.toAlignment ?: Alignment.Center
    )
}