package app.adon.suiengine.renderer.node

import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.Context
import app.adon.suiengine.renderer.extensions.register


val evalRegistryLogic: Map<String, EvalRegistryCaller> = buildMap {
    register("not") { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
        val boolNode = fn.paramSolver("value").get("value")?.evalAsBool(context, seen)
            ?: throw Exception("not function requires 1 parameter boolean")
        Node.Bool(!boolNode.v)
    }
    register("eq" ) { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
        val evaluatedNodes = fn.params.map { it.value.eval(context, seen) }
        if (evaluatedNodes.isEmpty()) throw Exception("eq function requires parameters")
        //
        val first = evaluatedNodes.first()
        val allEqual = evaluatedNodes.drop(1).all { it == first }
        Node.Bool(allEqual)
    }
    register("ter", "ternary", "if_else") { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
        val solver = fn.paramSolver("condition", "then", "else")
        val condNode = solver.get("condition")?.evalAsBool(context, seen)
            ?: throw Exception("ternary requires a boolean condition")
        if (condNode.v) {
            solver.get("then")?.eval(context, seen) ?: Node.Null
        } else {
            solver.get("else")?.eval(context, seen) ?: Node.Null
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

val evalRegistryLogics = mapOf(
    "not" to { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
        val boolNode = fn.paramSolver("value").get("value")?.evalAsBool(context, seen)
            ?: throw Exception("not function requires 1 parameter boolean")
        Node.Bool(!boolNode.v)
    },
    "eq" to { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
        val evaluatedNodes = fn.params.map { it.value.eval(context, seen) }
        if (evaluatedNodes.isEmpty()) throw Exception("eq function requires parameters")
        //
        val first = evaluatedNodes.first()
        val allEqual = evaluatedNodes.drop(1).all { it == first }
        Node.Bool(allEqual)
    },
    "ter" to { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
        val solver = fn.paramSolver("condition", "then", "else")
        val condNode = solver.get("condition")?.evalAsBool(context, seen)
            ?: throw Exception("ternary requires a boolean condition")
        if (condNode.v) {
            solver.get("then")?.eval(context, seen) ?: Node.Null
        } else {
            solver.get("else")?.eval(context, seen) ?: Node.Null
        }
    },
    "and" to { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
        var result = true
        iterateBool(fn.params, context, seen) { b ->
            result = result && b
        }
        Node.Bool(result)
    },
    "or" to { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
        var result = false
        iterateBool(fn.params, context, seen) { b ->
            result = result || b
        }
        Node.Bool(result)
    },
)

private fun iterateBool(params: List<Node.Param>, context: Context, seen: MutableSet<String>, caller: (Boolean) -> Unit) = params.forEach {
    val v = it.value.evalAs<Node.Bool>(context, seen)
        ?: throw Exception("boolean params must be bool")
    caller(v.v)
}