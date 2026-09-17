package app.adon.suiengine.renderer

import app.adon.suiengine.ast.Node
import app.adon.suiengine.ast.NodePathSegment
import app.adon.suiengine.renderer.node.evalAs
import app.adon.suiengine.renderer.node.evalAsInt

class StateStore(private val context: Context) {

    fun retrieveState(path: List<NodePathSegment>, seen: MutableSet<String> = mutableSetOf()): Node
    {
        if (path.isEmpty()) return Node.Null

        // O primeiro segmento deve ser obrigatoriamente a chave raiz (string)
        val rootKey = when (val rootSegment = path.first()) {
            is NodePathSegment.Property -> rootSegment.name
            is NodePathSegment.Index -> throw RuntimeException("A raiz do estado não pode ser um índice de array")
        }

        // Recupera a raiz (virtual primeiro, depois ViewModel)
        var currentNode: Node = context.retrieveVirtualState(rootKey) ?: run {
            val stableKey = context.findStableKey(rootKey)
            if (stableKey != null) context.vm.states[stableKey] else null
        } ?: throw RuntimeException("state [$rootKey] not found")

        // Navegação em profundidade (propriedades e índices)
        for (i in 1 until path.size) {
            currentNode = when (val segment = path[i]) {
                is NodePathSegment.Property -> {
                    val dict = currentNode.evalAs<Node.Dict>(context, seen)
                        ?: throw RuntimeException("cannot access property [${segment.name}] of a non-object state")

                    dict.v[segment.name]
                        ?: throw RuntimeException("property [${segment.name}] not found in state [$rootKey]")
                }
                is NodePathSegment.Index -> {
                    val arr = currentNode.evalAs<Node.Arr>(context, seen)
                        ?: throw RuntimeException("cannot access index of a non-array state")

                    val evaluatedIndexNode = segment.indexNode.evalAsInt(context, seen)
                        ?: throw RuntimeException("array index must be an integer")

                    val idx = evaluatedIndexNode.v
                    if (idx !in 0 until arr.v.size) {
                        throw RuntimeException("index out of bounds [$idx] for array in state [$rootKey]")
                    }

                    arr.v[idx]
                }
            }
        }

        return currentNode
    }

}