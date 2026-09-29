package app.adon.suiengine.renderer.renderer.props

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.toHorizontalAlignment
import app.adon.suiengine.renderer.extensions.toHorizontalArrangement
import app.adon.suiengine.renderer.extensions.toVerticalAlignment
import app.adon.suiengine.renderer.extensions.toVerticalArrangement
import app.adon.suiengine.renderer.node.eval.eval
import app.adon.suiengine.renderer.node.eval.evalToStr
import app.adon.suiengine.renderer.node.modifier.extractModifier
import app.adon.suiengine.renderer.node.paramSolver
import app.adon.suiengine.renderer.utils.takeAs

data class LazyRowProps(
    val array: Node.Arr,
    val asName: String,
    val indexName: String,
    val hArrange: Arrangement.Horizontal,
    val vAlign: Alignment.Vertical,
    val modifier: Modifier,
    val snap: Boolean,
    val children: List<Node>
) {

    companion object {
        fun from(fn: Node.Fn, context: Context): LazyRowProps {
            val ps = fn.paramSolver("items", "as", "index", "h_arrange", "v_align", "snap")
            //
            val rawListParam = ps.get("items")
                ?: throw Exception("[lazy_row] Requer um parâmetro 'items' com a coleção.")
            val arrayNode = rawListParam.eval(context).takeAs<Node.Arr>()
                ?: throw Exception("[lazy_row] O parâmetro 'items' deve ser um array ou avaliar para um Node.Arr.")

            val hArrange = ps.get("h_arrange")?.evalToStr(context)?.toHorizontalArrangement ?: Arrangement.Start
            val vAlign = ps.get("v_align")?.evalToStr(context)?.toVerticalAlignment ?: Alignment.Top
            val snap = ps.has("snap")
            val modifier = fn.extractModifier(context)

            //
            val asName = ps.get("as")?.evalToStr(context) ?: "item"
            val indexName = ps.get("index")?.evalToStr(context) ?: "index"

            return LazyRowProps(
                array = arrayNode,
                asName = asName,
                indexName = indexName,
                hArrange = hArrange,
                vAlign = vAlign,
                modifier = modifier,
                snap = snap,
                children = fn.children
            )
        }
    }

}