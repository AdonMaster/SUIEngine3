package app.adon.suiengine.renderer.renderer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import app.adon.suiengine.ast.Node
import app.adon.suiengine.ast.toNode
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.registerComponent
import app.adon.suiengine.renderer.node.eval.eval
import app.adon.suiengine.renderer.renderer.props.resolveForEachProps
import app.adon.suiengine.renderer.utils.takeAs


val renderRegistryFlow = buildMap<String, @Composable (Node.Fn, Context) -> Unit> {

    registerComponent(
        "if_chain",
        resolveProps = { node, context ->
            node.children.firstOrNull { b ->
                val fn = b as Node.Fn
                if (fn.name == "else") {
                    true
                } else {
                    val conditionResult = fn.params.first().value.eval(context)
                    val boolNode = conditionResult.takeAs<Node.Bool>()
                        ?: throw RuntimeException("[${fn.name}] requer parâmetro do tipo Boolean | ${conditionResult.stringableVal()}")
                    boolNode.v
                }
            } as? Node.Fn
        }
    ) { nodeToRender, context ->
        nodeToRender?.let {
            RenderGroup(it.children, context)
        }
    }

    registerComponent(
        "for_each", "foreach",
        resolveProps = { node, context -> node.resolveForEachProps(context) }
    ) { props, context ->
        props.items.forEachIndexed { idx, itemNode ->
            val loopContext = context.newChild("for_each")
            loopContext.setVirtual(props.asName, itemNode)
            loopContext.setVirtual(props.indexName, idx.toNode())

            key("${props.asName}_${idx}_${itemNode.uid}") {
                RenderGroup(props.children, loopContext)
            }
        }
    }

}
