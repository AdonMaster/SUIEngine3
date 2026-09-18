package app.adon.suiengine.renderer

import androidx.compose.runtime.Composable
import app.adon.suiengine.ast.Node


object FunctionRegistry {

    val renderers: Map<String, @Composable (Node.Fn, Context) -> Unit> = functionRegistryMisc + functionRegistryForm

    @Composable
    fun InvokeGroup(nodes: List<Node>, context: Context) {
        for (node in nodes.filterIsInstance<Node.Fn>()) {
            val fn = renderers[node.name]
            if (fn != null) {
                fn.invoke(node, context)
            } else {
                context.raise("Função não encontrada [${node.name}]")
            }
        }
    }

}