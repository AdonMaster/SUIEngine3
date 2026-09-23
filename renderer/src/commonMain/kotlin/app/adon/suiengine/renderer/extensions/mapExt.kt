package app.adon.suiengine.renderer.extensions

import androidx.compose.runtime.Composable
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context
import kotlin.coroutines.cancellation.CancellationException

fun <T> MutableMap<String, T>.register(
    vararg names: String,
    evaluator: T
) {
    for (name in names) {
        this[name] = evaluator
    }
}

inline fun <reified T> MutableMap<String, @Composable (Node.Fn, Context) -> Unit>.registerComponent(
    vararg names: String,
    crossinline resolveProps: (Node.Fn, Context) -> T,
    crossinline content: @Composable (props: T, Context) -> Unit
) {
    val renderLambda: @Composable (Node.Fn, Context) -> Unit = { node, context ->
        val props = try {
            resolveProps(node, context)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            context.raise(e)
            null
        }
        props?.let { p ->
            content(p, context)
        }
    }
    for (name in names) {
        this[name] = renderLambda
    }
}