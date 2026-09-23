package app.adon.suiengine.renderer.renderer.props

import androidx.compose.ui.Modifier
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.node.eval.evalToStr
import app.adon.suiengine.renderer.node.modifier.extractModifier
import app.adon.suiengine.renderer.node.paramSolver

data class TextProps(
    val modifier: Modifier,
    val text: String,
)

fun Node.Fn.resolveTextProps(context: Context): TextProps {
    val ps = paramSolver("text")
    val text = ps.get("text")?.evalToStr(context) ?: ""
    return TextProps(
        modifier = extractModifier(context),
        text = text
    )
}