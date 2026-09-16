package app.adon.suiengine.renderer.node

import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.Context

fun Node.eval(
    context: Context,
    seen: MutableSet<String> = mutableSetOf()
): Node {
    return when (val self = this) {
        // solved
        is Node.Bool,
        is Node.Str,
        is Node.Integer,
        is Node.Real,
        Node.Null -> self

        // structs
        is Node.Arr -> self.copy(v = self.v.map { it.eval(context, seen) })
        is Node.Dict -> self.copy(v = self.v.mapValues { (_, node) -> node.eval(context, seen) })

        // var
        is Node.Var -> {
            val pathKey = self.path.joinToString(".")
            if (!seen.add(pathKey)) {
                context.raise("eval: Referencia circular [${seen.joinToString(", ")}]")
                Node.Null
            } else {
                try {
                    context.retrieveState(self.path).eval(context, seen)
                } finally {
                    seen.remove(pathKey)
                }
            }
        }

        // function call
        is Node.Fn -> {
            EvalRegistry.execute(self, context, seen)
        }

        //
        is Node.Param -> {
            self.copy(value = self.value.eval(context, seen))
        }
    }
}

inline fun <reified T> Node.evalAs(context: Context, seen: MutableSet<String> = mutableSetOf()): T? {
    return this.eval(context, seen) as? T
}

fun Node.evalAsStrValue(context: Context, seen: MutableSet<String> = mutableSetOf()): String {
    return this.eval(context, seen).stringableVal()
}
fun Node.evalAsBool(context: Context, seen: MutableSet<String> = mutableSetOf()): Node.Bool? {
    return this.evalAs<Node.Bool>(context, seen)
}
fun Node.evalAsInt(context: Context, seen: MutableSet<String> = mutableSetOf()): Node.Integer? {
    return this.evalAs<Node.Integer>(context, seen)
}
fun Node.evalAsReal(context: Context, seen: MutableSet<String> = mutableSetOf()): Node.Real? {
    return this.evalAs<Node.Real>(context, seen)
}

//is Node.Bool,
//is Node.Str,
//is Node.Integer,
//is Node.Real,