package app.adon.suiengine.renderer.node

import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.Context

val evalRegistryConvert = mapOf<String, EvalRegistryCaller>(
    "toFloat" to { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
        val paramSolver = fn.paramSolver("value", "default")
        val default = paramSolver.get("default")?.evalAsStrValue(context, seen)?.toFloatOrNull()
            ?: 0f
        val value = paramSolver.get("value")?.evalAsStrValue(context, seen)?.toFloatOrNull()
            ?: default
        Node.Real(value)
    },
)