package app.adon.suiengine.renderer.node.curry

import app.adon.suiengine.ast.Node
import app.adon.suiengine.ast.toNode
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.register
import app.adon.suiengine.renderer.node.eval.eval

val nodeCurryRegistryConversions = buildMap<String, (Node.Fn, Node, Context, MutableSet<String>) -> Node> {

    register("toBool") { fn, node, context, seen ->
        val defaultVal = (fn.params.firstOrNull()?.value?.eval(context, seen) as? Node.Bool)?.v ?: false
        val res = when (val evaluatedNode = node.eval(context, seen)) {
            is Node.Bool -> evaluatedNode.v
            is Node.Number -> evaluatedNode.v != 0.0
            is Node.Str -> evaluatedNode.v.lowercase().let { it == "true" || it == "1" }
            else -> defaultVal
        }
        res.toNode()
    }


}