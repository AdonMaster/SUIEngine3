package app.adon.suiengine.renderer.node.eval

import app.adon.suiengine.ast.Node
import app.adon.suiengine.ast.toNode
import app.adon.suiengine.ast.toVarNode
import app.adon.suiengine.renderer.contexts.Context


object NodeEvalStr {

    private val REGEX_TEMPLATE = """(?<!\\)\$\{([a-zA-Z0-9_.\[\]]+)\}""".toRegex()

    fun resolve(node: Node.Str, context: Context, seen: MutableSet<String>): Node.Str {
        return renderTemplate(node.v) { key ->
            key.toVarNode().evalToStr(context, seen)
        }.toNode()
    }

    private fun renderTemplate(input: String, caller: (key: String) -> String): String {
        val interpolated = REGEX_TEMPLATE.replace(input) { matchResult ->
            val key = matchResult.groupValues[1]
            caller(key)
        }
        return interpolated.replace("""\\$""", "\${'$'}{")
    }

}