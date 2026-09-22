package app.adon.suiengine.renderer.renderer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import app.adon.suiengine.renderer.contexts.Context

@Composable
fun RenderEntry(contexts: List<Context>) {
    RenderGroup(contexts)
}

private val allRenderers = renderRegistryMisc + renderRegistryState


@Composable
fun RenderGroup(contexts: List<Context>) {
    contexts.forEachIndexed { index, context ->
        key(context.uid) {
            val renderer = allRenderers[context.name]
            if (renderer != null) {
                renderer.invoke(context)
            } else {
                context.raise("Componente <${context.name}>[$index] não encontrado")
                return@forEachIndexed
            }
        }
    }
}