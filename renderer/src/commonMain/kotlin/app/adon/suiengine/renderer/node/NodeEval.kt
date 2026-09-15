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
            if (!seen.add(self.name)) {
                context.raise("eval: Referencia circular [${seen.joinToString(", ")}]")
                Node.Null
            } else {
                context.retrieveState(self.name)
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
