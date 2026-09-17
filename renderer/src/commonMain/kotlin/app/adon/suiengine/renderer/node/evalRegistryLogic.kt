package app.adon.suiengine.renderer.node

import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.Context
import app.adon.suiengine.renderer.extensions.register


val evalRegistryLogic = buildMap {
    register("not") { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
        val bool = fn.paramSolver("value").get("value")?.resolveValBool(context, seen)
            ?: throw Exception("not function requires 1 parameter boolean")
        Node.Bool(!bool)
    }
    register("eq" ) { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
        val evaluatedNodes = fn.params.map { it.value.resolve(context, seen) }
        if (evaluatedNodes.isEmpty()) throw Exception("eq function requires parameters")
        //
        val first = evaluatedNodes.first()
        val allEqual = evaluatedNodes.drop(1).all { it == first }
        Node.Bool(allEqual)
    }
    register("ter", "ternary", "if_else") { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
        val solver = fn.paramSolver("condition", "then", "else")
        val condNode = solver.get("condition")?.resolveValBool(context, seen)
            ?: throw Exception("ternary requires a boolean condition")
        if (condNode) {
            solver.get("then")?.resolve(context, seen) ?: Node.Null
        } else {
            solver.get("else")?.resolve(context, seen) ?: Node.Null
        }
    }
    register("and") { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
        var result = true
        iterateBool(fn.params, context, seen) { b ->
            result = result && b
        }
        Node.Bool(result)
    }
    register("or" ) { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
        var result = false
        iterateBool(fn.params, context, seen) { b ->
            result = result || b
        }
        Node.Bool(result)
    }

}

private fun iterateBool(params: List<Node.Param>, context: Context, seen: MutableSet<String>, caller: (Boolean) -> Unit) = params.forEach {
    val v = it.value.resolveValBool(context, seen)
        ?: throw Exception("boolean params must be bool")
    caller(v)
}