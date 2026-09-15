package app.adon.suiengine.renderer.node

import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.Context

object EvalRegistry {

    fun execute(fn: Node.Fn, context: Context, seen: MutableSet<String>): Node {
        return when (fn.name) {
            "_concat" -> {
                val s = fn.params.joinToString("") {
                    it.value.eval(context, seen).stringableVal()
                }
                Node.Str(s)
            }
            else -> {
                context.raise("eval function [${fn.name}] não encontrado.")
                Node.Null
            }
        }
    }

}