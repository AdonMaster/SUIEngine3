package app.adon.suiengine.renderer.renderer.props

import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.node.eval.eval
import app.adon.suiengine.renderer.utils.takeAs

data class FormProps(
    val formName: String,
    val colProps: ColProps,
    val children: List<Node>,
    val rememberKey: String
)

fun Node.Fn.resolveFormProps(context: Context): FormProps {
    val name = params.firstOrNull()?.value?.eval(context)?.takeAs<Node.Str>()?.v
        ?: "default"
    val colProps = resolveColProps(context)

    val rememberKey = "${uid}_${name}"
    return FormProps(
        formName = name, colProps = colProps, children = this.children,
        rememberKey = rememberKey
    )
}