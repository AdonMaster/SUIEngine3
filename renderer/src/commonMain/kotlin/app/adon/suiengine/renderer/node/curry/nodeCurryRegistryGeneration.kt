package app.adon.suiengine.renderer.node.curry

import app.adon.suiengine.ast.Node
import app.adon.suiengine.ast.toNode
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.register
import app.adon.suiengine.renderer.node.eval.eval

val nodeCurryRegistryGeneration = buildMap<String, (Node.Fn, Node, Context, MutableSet<String>) -> Node> {

    register("repeat") { fn, node, context, seen ->
        val targetNode = node.eval(context, seen)
        val evaluatedTarget = targetNode.eval(context, seen)
        val countParam = fn.params.firstOrNull()?.value
            ?: throw Exception("[repeat] requer um parâmetro numérico ex: .repeat(3)")
        val numTimes = (countParam.eval(context, seen) as? Node.Number)
            ?: throw Exception("[repeat] o parâmetro precisa avaliar para um número")
        val times = numTimes.v.toInt().coerceAtLeast(0)
        val items = List(times) { evaluatedTarget }
        Node.Arr(v = items, extension = null)
    }

}