package app.adon.suiengine.renderer.extensions

import androidx.compose.runtime.Composable
import app.adon.suiengine.renderer.contexts.Context

fun <T> MutableMap<String, T>.register(
    vararg names: String,
    evaluator: T
) {
    for (name in names) {
        this[name] = evaluator
    }
}

inline fun <reified T> MutableMap<String, @Composable (Context) -> Unit>.registerComponent(
    vararg names: String,
    crossinline resolveProps: Context.() -> T,
    crossinline content: @Composable Context.(props: T) -> Unit
) {
    val renderLambda: @Composable (Context) -> Unit = { context ->
        runCatching { context.resolveProps() }
            .onSuccess { props ->
                context.content(props)
            }
            .onFailure { error ->
                context.raise(error)
            }
    }
    for (name in names) {
        this[name] = renderLambda
    }
}