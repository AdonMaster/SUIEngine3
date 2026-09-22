package app.adon.suiengine.renderer.node.eval

import app.adon.suiengine.ast.Node
import app.adon.suiengine.ast.NodePathSegment
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.node.attachExtension

object NodeEvalVar {

    fun resolve(node: Node.Var, context: Context, seen: MutableSet<String>): Node {
        val pathKey = node.stringableVal()
        if (!seen.add(pathKey)) {
            throw RuntimeException("eval: Referencia circular [${seen.joinToString(", ")}]")
        }
        try {
            val stateValue = context.getState(node.name)
            val resolvedBase = node.segments.fold(stateValue) { accumulator, element ->
                when (element) {
                    is NodePathSegment.Index -> {
                        val arr = (accumulator.eval(context, seen) as? Node.Arr)?.v
                            ?: throw RuntimeException("cannot access index of a non-array state")
                        val evalIndex = (element.indexNode.eval(context, seen) as? Node.Number)?.v?.toInt()
                            ?: throw RuntimeException("array index must be number")
                        if (evalIndex !in 0 until arr.size) {
                            throw RuntimeException("index out of bounds [$evalIndex] for array [${node.name}]")
                        }
                        arr[evalIndex]
                    }
                    is NodePathSegment.Property -> {
                        val dict = (accumulator.eval(context, seen) as? Node.Dict)?.v
                            ?: throw RuntimeException("cannot access property [${element.name}] of a non-object")
                        dict[element.name] ?:  throw RuntimeException("property [${element.name}] for object [${node.name}]")
                    }
                }
            }.eval(context, seen)

            //
            return if (node.extension != null) {
                resolvedBase.attachExtension(node.extension)
            } else {
                resolvedBase
            }
        } finally {
            seen.remove(pathKey)
        }
    }

}