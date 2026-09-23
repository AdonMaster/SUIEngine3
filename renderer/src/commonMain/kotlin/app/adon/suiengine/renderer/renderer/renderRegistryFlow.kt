package app.adon.suiengine.renderer.renderer

import androidx.compose.runtime.Composable
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.contexts.IfElseContext
import app.adon.suiengine.renderer.contexts.IfElseStatus
import app.adon.suiengine.renderer.contexts.IfElseType
import app.adon.suiengine.renderer.extensions.registerComponent
import app.adon.suiengine.renderer.node.eval.eval


val renderRegistryFlow = buildMap<String, @Composable (Node.Fn, Context) -> Unit> {

    registerComponent(
        "if",
        resolveProps = { node, context ->
            val pVal = node.params.firstOrNull()?.value
                ?: throw Exception("[if] requer uma condição")
            val condition = (pVal.eval(context) as? Node.Bool)
                ?: throw Exception("[if] param deve ser um boolean")
            condition.v to node.children.filterIsInstance<Node.Fn>()
        }
    ) { p, context ->
        val ifElseContext = context.newIfElseChild(IfElseType.IF, p.first)
        if (p.first) {
            RenderGroup(p.second, ifElseContext)
        }
    }

    registerComponent(
        "else_if", "else",
        resolveProps = { node, context ->
            val priorBranch = context.children.lastOrNull() as? IfElseContext
                ?: throw RuntimeException("[${node.name}] requer um 'if' ou 'else_if' anterior")

            if (priorBranch.type == IfElseType.ELSE) {
                throw RuntimeException("[${node.name}] não pode vir após um 'else'")
            }

            val status = if (priorBranch.resolved) {
                IfElseStatus.AlreadyResolved
            } else {
                if (node.name == "else") {
                    IfElseStatus.ElseForceResolve
                } else {
                    val p = node.params.firstOrNull()?.eval(context)
                        ?: throw RuntimeException("[${node.name}] parametro requerido")
                    if (p !is Node.Bool) throw RuntimeException("[${node.name}] requer parametro booleano")
                    IfElseStatus.NotResolved(p.v)
                }
            }

            val type = when (node.name) {
                "else_if" -> IfElseType.ELSE_IF
                "else" -> IfElseType.ELSE
                else -> IfElseType.IF
            }
            Triple(type, status, node.children.filterIsInstance<Node.Fn>())
        }
    ) { triple, context ->
        val (type, status, children) = triple
        when(status) {
            is IfElseStatus.NotResolved -> {
                val childContext = context.newIfElseChild(type, status.extracted)
                if (status.extracted) {
                    RenderGroup(children, childContext)
                }
            }
            IfElseStatus.AlreadyResolved -> {
                context.newIfElseChild(type, true)
            }
            IfElseStatus.ElseForceResolve -> {
                val childContext = context.newIfElseChild(type, true)
                RenderGroup(children, childContext)
            }
        }
    }


    //registerComponent(
    //    "for_each", "foreach",
    //    resolveProps = { resolveForEachProps() }
    //) { props ->
    //    props.items.forEachIndexed { idx, itemNode ->
    //        val loopContext = newChild()
    //        loopContext.setVirtual(props.asName, itemNode)
    //        loopContext.setVirtual(props.indexName, idx.toNode())
    //
    //        key(itemNode.uid) {
    //            RenderGroup(loopContext.children)
    //        }
    //    }
    //}

}
