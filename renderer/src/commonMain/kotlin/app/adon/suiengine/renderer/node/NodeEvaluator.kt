package app.adon.suiengine.renderer.node

import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.Context

data class NodeEvaluatorFnNotFound(val fn: Node.Fn) :
    Exception("node function [${fn.name}] not found.")

private val evalCollection = evalRegistryLogic + evalRegistryLayout + evalRegistryMath +
        evalRegistryConvert + evalRegistryMisc

fun Node.resolve(context: Context, seen: MutableSet<String> = mutableSetOf()): Node {
    return when (val self = this) {
        // primitive
        is Node.Bool,
        is Node.Str,
        is Node.Integer,
        is Node.Real,
        Node.Null -> self
        // structs
        is Node.Arr -> self.copy(v = self.v.map { it.resolve(context, seen) })
        is Node.Dict -> self.copy(v = self.v.mapValues { (_, n) -> n.resolve(context, seen) })
        is Node.Param -> self.copy(value = value.resolve(context, seen))
        // var
        is Node.Var -> {
            val pathKey = self.path.joinToString(".")
            if (!seen.add(pathKey)) {
                throw RuntimeException("eval: Referencia circular [${seen.joinToString(", ")}]")
            }
            try {
                context.unsafeRetrieveState(self.path).resolve(context, seen)
            } finally {
                seen.remove(pathKey)
            }
        }
        // function
        is Node.Fn -> {
            val evalFn = evalCollection[self.name] ?: throw NodeEvaluatorFnNotFound(self)
            evalFn(self, context, seen)
        }
    }
}


fun Node.resolveValStr(context: Context, seen: MutableSet<String> = mutableSetOf(), strict: Boolean = false, default: String = ""): String {
    val self = resolve(context, seen)
    if (strict) return (self as? Node.Str)?.v ?: default
    return self.stringableVal()
}

fun Node.resolveValInt(context: Context, seen: MutableSet<String> = mutableSetOf(), strict: Boolean = false, default: Int? = null): Int? {
    val self = resolve(context, seen)
    if (strict) return (self as? Node.Integer)?.v ?: default
    return self.stringableVal().toIntOrNull() ?: default
}

fun Node.resolveValFloat(context: Context, seen: MutableSet<String> = mutableSetOf(),strict: Boolean = true, default: Float? = null): Float? {
    val self = resolve(context, seen)
    if (strict) {
        return (self as? Node.Integer)?.v?.toFloat()
            ?: (self as? Node.Real)?.v
            ?: default
    }
    return self.stringableVal().toFloatOrNull() ?: default
}

fun Node.resolveValBool(context: Context, seen: MutableSet<String> = mutableSetOf(), strict: Boolean = true, default: Boolean? = null): Boolean? {
    val self = resolve(context, seen)
    if (strict) return (self as? Node.Bool)?.v ?: default
    return self.stringableVal().toBooleanStrictOrNull() ?: default
}
