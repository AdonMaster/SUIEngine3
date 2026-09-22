package app.adon.suiengine.renderer.renderer.props

import androidx.compose.ui.Modifier
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.node.eval.evalToStr
import app.adon.suiengine.renderer.node.modifier.extractModifier
import app.adon.suiengine.renderer.node.paramSolver

data class ButtonProps(
    val modifier: Modifier,
    val text: String?,
    val onTouch: Node?
)

fun Context.resolveButtonProps(): ButtonProps {
    val ps = node.paramSolver("text", "on_touch")
    return ButtonProps(
        modifier = node.extractModifier(this),
        text = ps.get("text")?.evalToStr(this),
        onTouch = ps.get("on_touch")
    )
}