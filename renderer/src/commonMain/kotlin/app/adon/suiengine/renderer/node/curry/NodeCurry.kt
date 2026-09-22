package app.adon.suiengine.renderer.node.curry

import app.adon.suiengine.ast.Node
import app.adon.suiengine.ast.NodeExtended
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.node.attachExtension
import app.adon.suiengine.renderer.node.eval.eval

object NodeCurry {

    private val allRegistries = nodeCurryRegistryLogical + nodeCurryRegistryMath +
            nodeCurryRegistryConversions + nodeCurryRegistryComparison +
            nodeCurryRegistryStr

    fun resolve(node: Node, context: Context, seen: MutableSet<String>): Node {
        val ext = (node as? NodeExtended)?.extension ?: return node
        val cleanNode = node.attachExtension(null)

        // registry
        val handler = allRegistries[ext.name]
            ?: throw Exception("Curry function [${ext.name}] não encontrada")

        // invoke
        val stepResult = handler.invoke(ext, cleanNode, context, seen)

        // prepare next
        val nextNode = if (ext.extension != null) {
            stepResult.attachExtension(ext.extension)
        } else {
            stepResult
        }

        //
        return nextNode.eval(context, seen)
    }

}
