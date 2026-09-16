package app.adon.suiengine.renderer.node

import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.Context
import app.adon.suiengine.renderer.node.evalAs
import app.adon.suiengine.renderer.utils.coalesce

typealias EvalRegistryCaller = (fn: Node.Fn, context: Context, seen: MutableSet<String>) -> Node
object EvalRegistry {

    val localRegistry = mapOf(
        "concat" to { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
            val s = fn.params.joinToString("") {
                it.value.eval(context, seen).stringableVal()
            }
            Node.Str(s)
        },
        "range" to { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
            val solver = fn.paramSolver("min", "max")
            val min = solver.get("min")?.evalAsInt(context, seen)?.v
                ?: throw Exception("range requires min integer")
            val max = solver.get("max")?.evalAsInt(context, seen)?.v
                ?: throw Exception("range requires max integer")
            val list = (min..max).map { Node.Integer(it) }
            Node.Arr(list)
        }
    )

    fun execute(fn: Node.Fn, context: Context, seen: MutableSet<String>): Node {
        return runCatching {
            val allRegistries = localRegistry + evalRegistryConvert +
                    evalRegistryMath + evalRegistryLogic
            val caller = allRegistries[fn.name]
                ?: throw Exception("eval function [${fn.name}] não encontrado.")
            return caller.invoke(fn, context, seen)
        }
            .onFailure { context.raise(it.message!!) }
            .getOrDefault(Node.Null)
    }

}


