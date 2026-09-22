package app.adon.suiengine.renderer.node.curry

import app.adon.suiengine.ast.Node
import app.adon.suiengine.ast.toNode
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.register
import app.adon.suiengine.renderer.node.eval.eval

val nodeCurryRegistryStr = buildMap<String, (Node.Fn, Node, Context, MutableSet<String>) -> Node> {

    register("concat") { fn, node, context, seen ->
        val targetNode = node.eval(context, seen)
        val initialText = targetNode.stringableVal()
        val paramsText = fn.params.joinToString(separator = "") { param ->
            param.value.eval(context, seen).stringableVal()
        }
        (initialText + paramsText).toNode()
    }

    register("upper", "uppercase") { _, node, context, seen ->
        node.eval(context, seen).stringableVal().uppercase().toNode()
    }

    register("lower", "lowercase") { _, node, context, seen ->
        node.eval(context, seen).stringableVal().lowercase().toNode()
    }

    register("trim") { _, node, context, seen ->
        node.eval(context, seen).stringableVal().trim().toNode()
    }

    register("contains") { fn, node, context, seen ->
        val targetText = node.eval(context, seen).stringableVal()
        val paramText = fn.params.firstOrNull()?.value?.eval(context, seen)?.stringableVal() ?: ""
        targetText.contains(paramText).toNode()
    }

}