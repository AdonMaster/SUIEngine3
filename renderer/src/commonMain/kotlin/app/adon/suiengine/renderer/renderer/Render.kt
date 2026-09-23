package app.adon.suiengine.renderer.renderer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context
import kotlinx.coroutines.NonCancellable.key

@Composable
fun RenderEntry(nodes: List<Node>, context: Context) {
    RenderGroup(nodes, context)
}

private val allRenderers = renderRegistryState + renderRegistryMisc + renderRegistryFlow +
        renderRegistryForm

@Composable
fun RenderGroup(nodes: List<Node>, context: Context) {

    val invalidNode = nodes.firstOrNull { it !is Node.Fn }
    if (invalidNode != null) {
        val nodeType = invalidNode::class.simpleName ?: "Desconhecido"
        val snippet = invalidNode.stringableVal()
        context.raise("Expressão ou valor solto [${nodeType}]: \"$snippet\". No SuiEngine, apenas chamadas de componentes (Fn) são permitidas em blocos de renderização.")
        return
    }

    nodes.filterIsInstance<Node.Fn>().forEachIndexed { index, node ->
        key(node.uid) {
            val renderer = allRenderers[node.name]
            if (renderer != null) {
                renderer.invoke(node, context)
            } else {
                context.raise("Componente <${node.name}>[$index] não encontrado")
                return@forEachIndexed
            }
        }
    }
}