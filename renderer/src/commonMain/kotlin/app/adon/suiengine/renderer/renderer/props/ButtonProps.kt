package app.adon.suiengine.renderer.renderer.props

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.toRGBA
import app.adon.suiengine.renderer.node.eval.evalToDouble
import app.adon.suiengine.renderer.node.eval.evalToStr
import app.adon.suiengine.renderer.node.modifier.extractModifier
import app.adon.suiengine.renderer.node.paramSolver

data class ButtonProps(
    val modifier: Modifier,
    val text: String?,
    val onTouch: Node?,
    val shape: RoundedCornerShape,
    val backgroundColor: Color?,
    val foregroundColor: Color?,
    val children: List<Node>,
    val elevation: Double,
)

fun Node.Fn.resolveButtonProps(context: Context): ButtonProps {
    val ps = paramSolver("text", "on_touch", "bg", "fg", "corner_radius", "elevation")
    val cornerRadius = ps.get("corner_radius")?.evalToDouble(context, "corner_radius") ?: 8.0
    val elevation = ps.get("elevation")?.evalToDouble(context, "elevation") ?: 0.0
    val shape = RoundedCornerShape(cornerRadius.dp)
    val backgroundColor = ps.get("bg")?.evalToStr(context)?.let { clStr ->
        clStr.toRGBA() ?: throw Exception("button.bg [$clStr] não é uma cor válida")
    }
    val foregroundColor = ps.get("fg")?.evalToStr(context)?.let { clStr ->
        clStr.toRGBA() ?: throw Exception("button.fg [$clStr] não é uma cor válida")
    }
    return ButtonProps(
        modifier = extractModifier(context, listOf("background", "bg", "fg")),
        text = ps.get("text")?.evalToStr(context),
        onTouch = ps.get("on_touch"),
        shape = shape,
        elevation = elevation,
        backgroundColor = backgroundColor,
        foregroundColor = foregroundColor,
        children = children
    )
}