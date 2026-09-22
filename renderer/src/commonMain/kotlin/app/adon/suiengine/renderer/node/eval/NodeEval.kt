package app.adon.suiengine.renderer.node.eval

import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.node.curry.NodeCurry

data class NodeEvalFnNotFound(val fn: Node.Fn): Exception("node function [${fn.name}] not found.")

fun Node.eval(context: Context, seen: MutableSet<String> = mutableSetOf()): Node {
    return when (val self = this) {
        // primitive
        is Node.Bool,
        is Node.Str,
        is Node.Number,
        Node.Null -> self

        //
        is Node.Arr -> self.copy(v = self.v.map { it.eval(context, seen) })
        is Node.Dict -> self.copy(v = self.v.mapValues { (_, n) -> n.eval(context, seen) })
        is Node.Param -> self.copy(value = value.eval(context, seen))

        //
        is Node.Var -> NodeEvalVar.resolve(this, context, seen)

        //
        is Node.Fn -> {
            //
            if (this.name == "default") {
                val targetParam = params.getOrNull(0)?.value
                if (targetParam == null) {
                    Node.Null
                } else {
                    val targetResult = runCatching {
                        targetParam.eval(context, seen)
                    }.getOrNull()
                    if (targetResult == null || targetResult is Node.Null) {
                        val fallbackParam = params.getOrNull(1)?.value
                        fallbackParam?.eval(context, seen) ?: Node.Null
                    } else {
                        targetResult
                    }
                }
            } else {
                throw NodeEvalFnNotFound(this)
            }
        }

    }.let { NodeCurry.resolve(it, context, seen) }
}

fun Node.evalToStr(context: Context, seen: MutableSet<String> = mutableSetOf()) = this
    .eval(context, seen)
    .stringableVal()