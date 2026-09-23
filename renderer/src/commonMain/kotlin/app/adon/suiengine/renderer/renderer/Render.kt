package app.adon.suiengine.renderer.renderer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context
import kotlinx.coroutines.NonCancellable.key

@Composable
fun RenderEntry(nodes: List<Node.Fn>, context: Context) {
    RenderGroup(nodes, context)
}

private val allRenderers = renderRegistryState + renderRegistryMisc + renderRegistryFlow

@Composable
fun RenderGroup(fns: List<Node.Fn>, context: Context) {
    fns.forEachIndexed { index, node ->
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