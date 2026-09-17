package app.adon.suiengine.renderer.node

import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.Context
import app.adon.suiengine.renderer.StateStoreKeyNotFoundException
import app.adon.suiengine.renderer.extensions.register

val evalRegistryConvert = buildMap {
    register("default", "isnull") { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
        val paramSolver = fn.paramSolver("value", "default")
        val valueParam = paramSolver.get("value") ?: throw RuntimeException("default function requires [value] param")
        val defaultParam = paramSolver.get("default") ?: throw RuntimeException("default function requires [default] param")
        val value = try {
            valueParam.resolve(context, seen)
        } catch (_: StateStoreKeyNotFoundException) {
            Node.Null
        }
        val default = defaultParam.resolve(context, seen)
        value.takeIf { it != Node.Null } ?: default
    }
    register("float") { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
        val paramSolver = fn.paramSolver("value", "default")
        val default = paramSolver.get("default")?.resolveValFloat(context, seen)
            ?: 0f
        val value = paramSolver.get("value")?.resolveValFloat(context, seen, default = default)
        Node.Real(value!!)
    }
}