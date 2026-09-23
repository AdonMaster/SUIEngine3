package app.adon.suiengine.renderer.renderer.props

import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.node.eval.eval
import app.adon.suiengine.renderer.node.eval.evalToStr
import app.adon.suiengine.renderer.node.paramSolver

data class ForEachProps(
    val items: List<Node>,
    val asName: String,
    val indexName: String,
    val children: List<Node>
)

fun Node.Fn.resolveForEachProps(context: Context): ForEachProps {
    val paramSolver = paramSolver("items", "as", "index")

    // 1. Resolve e valida a lista
    val rawListParam = paramSolver.get("items")
        ?: throw Exception("[for_each] Requer um parâmetro 'items' com a coleção.")

    val arrayNode = (rawListParam.eval(context) as? Node.Arr)
        ?: throw Exception("[for_each] O parâmetro 'items' deve ser um array ou avaliar para um Node.Arr.")

    //
    val asName = paramSolver.get("as")?.evalToStr(context) ?: "item"
    val indexName = paramSolver.get("index")?.evalToStr(context) ?: "index"

    return ForEachProps(
        items = arrayNode.v,
        asName = asName,
        indexName = indexName,
        children = children
    )
}