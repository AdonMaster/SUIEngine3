package app.adon.suiengine.renderer.node

import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.Context
import app.adon.suiengine.renderer.node.evalAs

object EvalRegistry {

    fun execute(fn: Node.Fn, context: Context, seen: MutableSet<String>): Node {
        return runCatching {
            when (fn.name) {
                "sum" -> {
                    val i = fn.params
                        .fold(0) { acc, node ->
                            val ii = node.value.evalAs<Node.Integer>(context, seen)
                                ?: throw Exception("sum params must be int")
                            acc + ii.v
                        }
                    Node.Integer(i)
                }
                "concat" -> {
                    val s = fn.params.joinToString("") {
                        it.value.eval(context, seen).stringableVal()
                    }
                    Node.Str(s)
                }
                else -> {
                    throw Exception("eval function [${fn.name}] não encontrado.")
                }
            }
        }
            .onFailure { context.raise(it.message!!) }
            .getOrDefault(Node.Null)
    }

}