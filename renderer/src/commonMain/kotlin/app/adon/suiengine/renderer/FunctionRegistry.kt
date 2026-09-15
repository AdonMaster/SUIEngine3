package app.adon.suiengine.renderer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.node.NodeParamSolver
import app.adon.suiengine.renderer.node.eval


typealias FnRenderer = @Composable (Node.Fn, Context) -> Unit
object FunctionRegistry {

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

    private val renderers = mapOf<String, FnRenderer>(

        "@state" to { node, context ->
            node.params.forEach { param ->
                context.storeState(node.uid, param.name, param.value)
            }
        },

        "@if" to { node, context ->
            InvokeGroup(node.children, context.newChild(node.name))
        },

        "@set" to { node, context ->

        },

        "box" to { node, context ->
            Box(modifier = Modifier) {
                InvokeGroup(node.children, context.newChild(node.name))
            }
        },

        "col" to { node, context ->
            Column {
                InvokeGroup(node.children, context.newChild(node.name))
            }
        },

        "row" to { node, context ->
            Row {
                InvokeGroup(node.children, context.newChild(node.name))
            }
        },

        "spacer" to { node, context ->
            Spacer(modifier = Modifier.size(4.dp))
        },

        "divider" to { node, context ->
            HorizontalDivider(modifier = Modifier)
        },

        "text" to { node, context ->
            val paramSolver = NodeParamSolver(node.params, listOf("text"))
            val text = paramSolver.get("text")?.eval(context)?.stringableVal() ?: ""
            Text(text = text, modifier = Modifier.padding(20.dp))
        },

        "btn" to { node, context ->
            // params
            val paramSolver = NodeParamSolver(node.params, listOf("text"))
            val textValue = paramSolver.get("text")?.eval(context)?.stringableVal()
            Button(
                onClick = {
//                    node.childrenFnByNameAndSingleParamValue("@on", "touch")
//                        .flatMap { it.childrenFnByName("@set") }
//                        .flatMap { it.params }
//                        .forEach { param ->
//                            context.safeStoreState(param.name, param.value.eval(context))
//                        }
                }
            ) {
                if (textValue != null) {
                    Text(textValue)
                } else {
                    InvokeGroup(node.children, context.newChild(node.name))
                }
            }
        }
    )

}