package app.adon.suiengine.renderer.node

import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.Context

val evalRegistryMath = mapOf(
    "mod" to { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
        val solver = fn.paramSolver("a", "b")
        val a = solver.get("a")?.resolveValInt(context, seen)
            ?: throw Exception("mod function requires int as first parameter")
        val b: Int = solver.get("b")?.resolveValInt(context, seen)
            ?: throw Exception("mod function requires int as second parameter")
        Node.Integer(a.mod(b))
    },
    "mul" to { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
        var i = 1
        iterateInt("mul", fn.params, context, seen) { i *= it }
        Node.Integer(i)
    },
    "sub" to { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
        var i = 0
        iterateInt("sub", fn.params, context, seen) { i -= it }
        Node.Integer(i)
    },
    "sum" to { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
        var i = 0
        iterateInt("sum", fn.params, context, seen) { i += it }
        Node.Integer(i)
    },
)

private fun iterateInt(fnName: String, params: List<Node.Param>, context: Context, seen: MutableSet<String>, caller: (Int)->Unit) = params.forEach {
    val v = it.value.resolveValInt(context, seen)
        ?: throw Exception("$fnName params must be int")
    caller(v)
}