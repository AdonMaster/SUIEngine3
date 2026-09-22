package app.adon.suiengine.renderer.node.curry

import app.adon.suiengine.ast.Node
import app.adon.suiengine.ast.toNode
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.register
import app.adon.suiengine.renderer.node.eval.eval

val nodeCurryRegistryLogical = buildMap<String, (Node.Fn, Node, Context, MutableSet<String>) -> Node> {

    register("and") { fn, node, context, seen ->
        val (a, b) = extractBoolAB(fn, node, context, seen)
        (a && b).toNode()
    }

    register("or") { fn, node, context, seen ->
        val (a, b) = extractBoolAB(fn, node, context, seen)
        (a || b).toNode()
    }

    register("eq") { fn, node, context, seen ->
        val targetNode = node.eval(context, seen)
        val rawParam = fn.params.firstOrNull()?.value
            ?: throw Exception("Operation 'eq' requires a parameter to compare")
        val paramNode = rawParam.eval(context, seen)
        areNodesEqual(targetNode, paramNode).toNode()
    }

    register("not") { _, node, context, seen ->
        val a = (node.eval(context, seen) as? Node.Bool)?.v
            ?: throw Exception("Operation 'not' target must be a boolean")
        (!a).toNode()
    }

    register("pick", "then_else", "then") { fn, node, context, seen ->
        val isTrue = when (val conditionNode = node.eval(context, seen)) {
            is Node.Bool -> conditionNode.v
            else -> conditionNode.stringableVal().isNotBlank()
        }

        val trueParam = fn.params.getOrNull(0)?.value
            ?: throw IllegalArgumentException("A função '${fn.name}' exige pelo menos o parâmetro para 'true'.")
        val falseParam = fn.params.getOrNull(1)?.value

        if (isTrue) {
            trueParam.eval(context, seen)
        } else {
            falseParam?.eval(context, seen) ?: Node.Null
        }
    }
}

private fun extractBoolAB(
    fn: Node.Fn,
    node: Node,
    context: Context,
    seen: MutableSet<String>
): Pair<Boolean, Boolean> {
    val a = (node.eval(context, seen) as? Node.Bool)?.v
        ?: throw Exception("Operation '${fn.name}' target must be a boolean")
    val rawParam = fn.params.firstOrNull()?.value
        ?: throw Exception("Operation '${fn.name}' requires a parameter")
    val b = (rawParam.eval(context, seen) as? Node.Bool)?.v
        ?: throw Exception("Operation '${fn.name}' parameter must be a boolean")
    return a to b
}

fun areNodesEqual(a: Node, b: Node): Boolean {
    return when (a) {
        is Node.Number if b is Node.Number -> a.v == b.v
        is Node.Str if b is Node.Str -> a.v == b.v
        is Node.Bool if b is Node.Bool -> a.v == b.v
        is Node.Null if b is Node.Null -> true
        is Node.Arr if b is Node.Arr -> {
            if (a.v.size != b.v.size) false
            else a.v.zip(b.v).all { (itemA, itemB) -> areNodesEqual(itemA, itemB) }
        }

        is Node.Dict if b is Node.Dict -> {
            if (a.v.size != b.v.size) false
            else a.v.keys == b.v.keys && a.v.all { (k, vA) -> b.v[k]?.let { vB -> areNodesEqual(vA, vB) } == true }
        }

        else -> false
    }
}