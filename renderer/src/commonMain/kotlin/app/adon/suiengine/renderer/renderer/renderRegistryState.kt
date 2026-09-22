package app.adon.suiengine.renderer.renderer

import androidx.compose.runtime.Composable
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.register

val renderRegistryState = buildMap<String, @Composable (Context) -> Unit> {

    register("declare", "state") { context ->
        try {
            context.node.params.forEach { param ->
                val key = param.name ?: throw Exception("[declare] requires named params")
                context.parent?.initState(key, param.value)
            }
        } catch (e: Exception) {
            context.raise(e)
        }
    }

}