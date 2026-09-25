package app.adon.suiengine.renderer.invoker

import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.node.eval.NodeEvalFnNotFound
import app.adon.suiengine.renderer.node.eval.eval

object Invoker {

    fun trigger(node: Node?, context: Context) {
        val resolvedNode = node ?: return
        try {
            triggerNode(resolvedNode, context)
        } catch (e: Exception) {
            context.raise(e.message ?: "invokeRegistry@err")
        }
    }

    //
    private fun triggerNode(n: Node, context: Context, seen: MutableSet<String> = mutableSetOf()) {
        val fnNode = try {
            n.eval(context, seen)
        } catch (e: NodeEvalFnNotFound) {
            e.fn
        }
        when (fnNode) {
            is Node.Arr -> {
                fnNode.v.forEach {
                    triggerNode(it, context, seen)
                }
            }
            is Node.Fn -> {
                when (fnNode.name) {
                    "set" -> {
                        fnNode.params.forEach { param ->
                            val key = param.name ?: "set requer o nome da variavel"
                            val evaluatedValue = param.value.eval(context, seen)
                            context.setState(key, evaluatedValue)
                        }
                    }
                    "nop" -> {}
                    else -> throw RuntimeException("can't invoke the following: [${fnNode.name}]")
                }
            }
            else -> throw RuntimeException("can't invoke the following: [${fnNode.stringableVal()}]")
        }
    }

}