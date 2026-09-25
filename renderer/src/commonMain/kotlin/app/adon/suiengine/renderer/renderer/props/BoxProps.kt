package app.adon.suiengine.renderer.renderer.props

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.toAlignment
import app.adon.suiengine.renderer.node.eval.evalToStr
import app.adon.suiengine.renderer.node.modifier.extractModifier
import app.adon.suiengine.renderer.node.paramSolver

data class BoxProps(
    val modifier: Modifier,
    val contentAlignment: Alignment?,
    val children: List<Node.Fn>
)

fun Node.Fn.resolveBoxProps(context: Context): BoxProps {
    val ps = paramSolver("content_align")
    val contentAlignment = ps.get("content_align")?.let {
        val ss = it.evalToStr(context)
        ss.toAlignment ?: throw RuntimeException("[align] não reconhece [$ss]")
    }

    return BoxProps(
        modifier = extractModifier(context),
        contentAlignment = contentAlignment,
        children = children.filterIsInstance<Node.Fn>()
    )
}