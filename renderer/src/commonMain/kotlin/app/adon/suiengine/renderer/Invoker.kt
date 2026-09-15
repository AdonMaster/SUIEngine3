package app.adon.suiengine.renderer

import androidx.compose.runtime.Composable
import app.adon.suiengine.ast.Node

@Composable
fun InvokeGroup(nodes: List<Node>) {
    InvokeGroup(nodes, null)
}

@Composable
private fun InvokeGroup(nodes: List<Node>, parentContext: Context?) {
    val context = Context(
        parent = parentContext
    )
    for (n in nodes.filterIsInstance<Node.Fn>()) {
        Invoke(n, context)
    }
}

@Composable
private fun Invoke(node: Node.Fn, parentContext: Context?) {

}