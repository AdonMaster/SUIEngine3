package app.adon.suiengine.renderer.node

import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.Context

object InvokeRegistry {

    fun trigger(node: Node?, context: Context, seen: MutableSet<String> = mutableSetOf()) {
        val resolvedNode = node ?: return
        try {
            triggerNode(resolvedNode, context, seen)
        } catch (e: Exception) {
            context.raise(e.message ?: "invokeRegistry@err")
        }
    }

    //
    private fun triggerNode(n: Node, context: Context, seen: MutableSet<String> = mutableSetOf()) {
        val fnNode = try {
            n.resolve(context, seen)
        } catch (e: NodeEvaluatorFnNotFound) {
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
                    "@set" -> {
                        fnNode.params.forEach { param ->
                            val evaluatedValue = param.value.resolve(context, seen)
                            context.unsafeStoreState(param.name, evaluatedValue)
                        }
                    }
                    else -> throw RuntimeException("can't invoke the following: [${fnNode.name}]")
                }
            }
            else -> throw RuntimeException("can't invoke the following: [${fnNode.stringableVal()}]")
        }
    }
}

