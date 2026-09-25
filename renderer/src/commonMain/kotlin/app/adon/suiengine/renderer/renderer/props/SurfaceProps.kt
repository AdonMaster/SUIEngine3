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

data class SurfaceProps(
    val cornerRadius: Double,
    val elevation: Double,
    val shape: RoundedCornerShape,
    val backgroundColor: Color?,
    val foregroundColor: Color?,
    val onTouch: Node?,
    val modifier: Modifier,
    val children: List<Node>
) {
    companion object {
        fun from(node: Node.Fn, context: Context): SurfaceProps {
            val paramSolver = node.paramSolver(
                "corner_radius", "bg", "fg", "on_touch",
                "elevation", "clickable"
            )
            val cornerRadius = paramSolver.get("corner_radius")?.evalToDouble(context, "corner_radius") ?: 8.0
            val elevation = paramSolver.get("elevation")?.evalToDouble(context, "elevation") ?: 0.0
            val shape = RoundedCornerShape(cornerRadius.dp)
            val backgroundColor = paramSolver.get("bg")?.evalToStr(context)?.let { clStr ->
                clStr.toRGBA() ?: throw Exception("surface.bg [$clStr] não é uma cor válida")
            }
            val foregroundColor = paramSolver.get("fg")?.evalToStr(context)?.let { clStr ->
                clStr.toRGBA() ?: throw Exception("surface.fg [$clStr] não é uma cor válida")
            }
            val onTouch = paramSolver.get("on_touch")

            val modifier = node.extractModifier(
                context,
                listOf("background", "foreground", "corner_radius", "elevation", "on_touch", "clickable")
            )

            return SurfaceProps(
                cornerRadius = cornerRadius,
                elevation = elevation,
                shape = shape,
                backgroundColor = backgroundColor,
                foregroundColor = foregroundColor,
                onTouch = onTouch,
                modifier = modifier,
                children = node.children
            )
        }
    }
}