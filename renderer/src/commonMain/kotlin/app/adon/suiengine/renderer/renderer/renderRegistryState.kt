package app.adon.suiengine.renderer.renderer

import androidx.compose.runtime.Composable
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.register

val renderRegistryState = buildMap<String, @Composable (node: Node.Fn, context: Context) -> Unit> {

    register("declare", "state") { node, context ->
        try {
            node.params.forEach { param ->
                val key = param.name ?: throw Exception("[declare] requires named params")
                context.initState(node.uid, key, param.value)
            }
        } catch (e: Exception) {
            context.raise(e)
        }
    }

}