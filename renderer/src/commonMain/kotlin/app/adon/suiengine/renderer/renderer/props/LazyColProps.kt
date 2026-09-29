package app.adon.suiengine.renderer.renderer.props

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.toHorizontalAlignment
import app.adon.suiengine.renderer.extensions.toVerticalArrangement
import app.adon.suiengine.renderer.node.eval.eval
import app.adon.suiengine.renderer.node.eval.evalToStr
import app.adon.suiengine.renderer.node.modifier.extractModifier
import app.adon.suiengine.renderer.node.paramSolver
import app.adon.suiengine.renderer.utils.takeAs

data class LazyColProps(
    val array: Node.Arr,
    val asName: String,
    val indexName: String,
    val vArrange: Arrangement.Vertical,
    val hAlign: Alignment.Horizontal,
    val modifier: Modifier,
    val children: List<Node>
) {

    companion object {
        fun from(fn: Node.Fn, context: Context): LazyColProps {
            val ps = fn.paramSolver("items", "as", "index", "v_arrange", "h_align")
            //
            val rawListParam = ps.get("items")
                ?: throw Exception("[lazy_col] Requer um parâmetro 'items' com a coleção.")
            val arrayNode = rawListParam.eval(context).takeAs<Node.Arr>()
                ?: throw Exception("[lazy_col] O parâmetro 'items' deve ser um array ou avaliar para um Node.Arr.")

            val vArrange = ps.get("v_arrange")?.evalToStr(context)?.toVerticalArrangement ?: Arrangement.Top
            val hAlign = ps.get("h_align")?.evalToStr(context)?.toHorizontalAlignment ?: Alignment.Start
            val modifier = fn.extractModifier(context)

            //
            val asName = ps.get("as")?.evalToStr(context) ?: "item"
            val indexName = ps.get("index")?.evalToStr(context) ?: "index"

            return LazyColProps(
                array = arrayNode,
                asName = asName,
                indexName = indexName,
                vArrange = vArrange,
                hAlign = hAlign,
                modifier = modifier,
                children = fn.children
            )
        }
    }

}