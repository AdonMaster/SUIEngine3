package app.adon.suiengine.renderer.renderer

import androidx.compose.runtime.Composable
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context

val renderRegistryLazyCol = buildMap<String, @Composable (Node.Fn, Context) -> Unit> {

}