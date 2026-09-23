package app.adon.suiengine.renderer.node.curry

import app.adon.suiengine.ast.Node
import app.adon.suiengine.ast.toNode
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.register
import app.adon.suiengine.renderer.node.eval.eval

val nodeCurryRegistryArr = buildMap<String, (Node.Fn, Node, Context, MutableSet<String>) -> Node> {

    register("length") { fn, subject, context, seen ->
        when (val resolved = subject.eval(context, seen)) {
            is Node.Arr -> resolved.v.size.toNode()
            is Node.Bool -> 1.toNode()
            is Node.Dict -> resolved.v.keys.size.toNode()
            is Node.Fn -> 1.toNode()
            Node.Null -> 0.toNode()
            is Node.Number -> resolved.v.toInt().toNode()
            is Node.Param -> 1.toNode()
            is Node.Str -> resolved.v.length.toNode()
            is Node.Var -> 0.toNode()
        }
    }

    register("push") { fn, subject, context, seen ->
        val resolved = subject.eval(context, seen) as? Node.Arr
            ?: throw Exception("Operation 'push' requires an Array target")

        if (fn.params.isEmpty()) throw Exception("Operation 'push' requires at least one item to push")

        val newItems = fn.params.map { it.value.eval(context, seen) }
        Node.Arr(resolved.v + newItems, extension = null)
    }

    register("removeAt") { fn, subject, context, seen ->
        val resolved = subject.eval(context, seen) as? Node.Arr
            ?: throw Exception("Operation 'removeAt' requires an Array target")

        val rawParam = fn.params.firstOrNull()?.value
            ?: throw Exception("Operation 'removeAt' requires an index parameter")

        val indexNode = rawParam.eval(context, seen) as? Node.Number
            ?: throw Exception("Parameter for 'removeAt' must evaluate to a Number")

        val index = indexNode.v.toInt()
        if (index !in resolved.v.indices) {
            throw Exception("Index $index out of bounds for array of size ${resolved.v.size}")
        }

        val newList = resolved.v.toMutableList().apply { removeAt(index) }
        Node.Arr(newList, extension = null)
    }

    register("first") { fn, subject, context, seen ->
        val resolved = subject.eval(context, seen) as? Node.Arr
            ?: throw Exception("Operation 'first' requires an Array target")

        resolved.v.firstOrNull() ?: Node.Null
    }

    register("last") { fn, subject, context, seen ->
        val resolved = subject.eval(context, seen) as? Node.Arr
            ?: throw Exception("Operation 'last' requires an Array target")

        resolved.v.lastOrNull() ?: Node.Null
    }

    register("join") { fn, subject, context, seen ->
        val resolved = subject.eval(context, seen) as? Node.Arr
            ?: throw Exception("Operation 'join' requires an Array target")

        val separatorParam = fn.params.firstOrNull()?.value?.eval(context, seen) as? Node.Str
        val separator = separatorParam?.v ?: ", "

        val joinedString = resolved.v.joinToString(separator) { it.stringableVal() }
        joinedString.toNode()
    }

}