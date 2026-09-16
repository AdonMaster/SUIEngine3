package app.adon.suiengine.renderer.node

import app.adon.suiengine.ast.Node

class NodeParamSolver(
    private val params: List<Node.Param>, private val names: List<String>
) {
    inline fun <reified T: Node> getTyped(name: String): T? {
        return find(name)?.value as? T
    }
    fun get(name: String): Node? {
        return find(name)?.value
    }

    fun find(name: String): Node.Param? {
        // named, easy-peasy
        params.firstOrNull { it.name == name }?.let { return it }

        //
        val supposedIndex = names.indexOf(name)
        if (supposedIndex < 0) throw RuntimeException("param $name não registrado")

        val anonymousParams = params.filter { it.name == null }
        val namedKeysProvided = params.filter { it.name != null }.map { it.name }.toSet()
        val precedingNames = names.subList(0, supposedIndex)
        val stolenSlots = precedingNames.count { it in namedKeysProvided }
        val targetIndex = supposedIndex - stolenSlots
        return anonymousParams.getOrNull(targetIndex)
    }
}

fun Node.Fn.paramSolver(vararg names: String) = NodeParamSolver(this.params, names.toList())