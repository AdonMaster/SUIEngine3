package app.adon.suiengine.renderer.node

import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.Context

val evalRegistryMisc = mapOf(
    "concat" to { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
        val s = fn.params.joinToString("") {
            it.value.resolveValStr(context, seen)
        }
        Node.Str(s)
    },
    "range" to { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
        val solver = fn.paramSolver("min", "max")
        val min = solver.get("min")?.resolveValInt(context, seen)
            ?: throw Exception("range requires min integer")
        val max = solver.get("max")?.resolveValInt(context, seen)
            ?: throw Exception("range requires max integer")
        val list = (min..max).map { Node.Integer(it) }
        Node.Arr(list)
    },
    "array_len" to { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
        val arr = fn.params.firstOrNull()?.value?.resolve(context, seen) as? Node.Arr
            ?: throw RuntimeException("array_len requires an array as only parameter")
        Node.Integer(arr.v.size)
    }
)