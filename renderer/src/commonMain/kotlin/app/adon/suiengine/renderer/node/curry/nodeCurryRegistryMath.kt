package app.adon.suiengine.renderer.node.curry

import app.adon.suiengine.ast.Node
import app.adon.suiengine.ast.toNode
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.register
import app.adon.suiengine.renderer.node.eval.eval
import kotlin.math.pow

val nodeCurryRegistryMath = buildMap<String, (Node.Fn, Node, Context, MutableSet<String>) -> Node> {

    register("add", "sum") { fn, node, context, seen ->
        val (a, b) = extractNumAB(fn, node, context, seen)
        evalMath(a, b) { x, y -> x + y }
    }

    register("sub") { fn, node, context, seen ->
        val (a, b) = extractNumAB(fn, node, context, seen)
        evalMath(a, b) { x, y -> x - y }
    }

    register("mul") { fn, node, context, seen ->
        val (a, b) = extractNumAB(fn, node, context, seen)
        evalMath(a, b) { x, y -> x * y }
    }

    register("div") { fn, node, context, seen ->
        val (a, b) = extractNumAB(fn, node, context, seen)
        if (b.v == 0.0) throw Exception("Division by zero")
        val isExact = (a.v % b.v == 0.0)
        if (!a.hasDigits && !b.hasDigits && isExact) {
            (a.v.toInt() / b.v.toInt()).toNode()
        } else {
            (a.v / b.v).toNode()
        }
    }

    register("pow") { fn, node, context, seen ->
        val (a, b) = extractNumAB(fn, node, context, seen)
        if (b.v < 0) {
            a.v.pow(b.v).toNode()
        } else {
            evalMath(a, b) { x, y -> x.pow(y) }
        }
    }

    register("mod") { fn, node, context, seen ->
        val (a, b) = extractNumAB(fn, node, context, seen)
        if (b.v == 0.0) throw Exception("Modulo by zero")
        evalMath(a, b) { x, y -> x % y }
    }

    // comparison

}

fun extractNumAB(fn: Node.Fn, node: Node, context: Context, seen: MutableSet<String>): Pair<Node.Number, Node.Number> {
    val a = (node.eval(context, seen) as? Node.Number)
        ?: throw Exception("[${fn.name}] only works with numbers")
    val b = (fn.params.firstOrNull()?.value?.eval(context, seen) as? Node.Number)
        ?: throw Exception("[${fn.name}] requires a number param")
    return a to b
}

private inline fun evalMath(
    nodeA: Node.Number,
    nodeB: Node.Number,
    op: (Double, Double) -> Double
): Node.Number {
    val resultDouble = op(nodeA.v, nodeB.v)
    val isIntOperation = !nodeA.hasDigits && !nodeB.hasDigits
    return if (isIntOperation) {
        resultDouble.toInt().toNode()
    } else {
        resultDouble.toNode()
    }
}