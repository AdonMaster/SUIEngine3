package app.adon.suiengine.renderer.node.curry

import app.adon.suiengine.ast.Node
import app.adon.suiengine.ast.toNode
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.register
import app.adon.suiengine.renderer.node.eval.eval

val nodeCurryRegistryComparison = buildMap<String, (Node.Fn, Node, Context, MutableSet<String>) -> Node> {

    register("gt") { fn, node, context, seen ->
        val (a, b) = extractNumAB(fn, node, context, seen)
        (a.v > b.v).toNode()
    }
    register("gte") { fn, node, context, seen ->
        val (a, b) = extractNumAB(fn, node, context, seen)
        (a.v >= b.v).toNode()
    }
    register("lt") { fn, node, context, seen ->
        val (a, b) = extractNumAB(fn, node, context, seen)
        (a.v < b.v).toNode()
    }
    register("lte") { fn, node, context, seen ->
        val (a, b) = extractNumAB(fn, node, context, seen)
        (a.v <= b.v).toNode()
    }
    register("neq") { fn, node, context, seen ->
        val targetNode = node.eval(context, seen)
        val rawParam = fn.params.firstOrNull()?.value
            ?: throw Exception("Operation 'neq' requires a parameter to compare")
        val paramNode = rawParam.eval(context, seen)

        (!areNodesEqual(targetNode, paramNode)).toNode()
    }
}