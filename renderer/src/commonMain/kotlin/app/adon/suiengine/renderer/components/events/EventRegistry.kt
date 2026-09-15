package app.adon.suiengine.renderer.components.events

import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.Context
import app.adon.suiengine.renderer.extensions.toEnum
import app.adon.suiengine.renderer.node.NodeParamSolver

enum class EventFilter {
    TOUCH
}
object EventRegistry {

    fun trigger(filter: EventFilter, node: Node.Fn, context: Context) {
        runSingle(filter, node, context, mutableListOf())
    }

    private fun runSingle(filter: EventFilter, node: Node, context: Context, acc: List<Node.Fn>) {
        val fn = node as? Node.Fn
        if (fn == null) {
            context.raise("Apenas funcoes sao suportadas para eventos")
            return
        }

        //
        runCatching {
            when (fn.name) {
                "@on" -> {
                    if (acc.isNotEmpty()) throw Exception("@on event nao deve estar dentro de outro evento")
                    val paramSolver = NodeParamSolver(fn.params, listOf("eventName"))
                    val eventName = paramSolver.get("eventName")
                        ?: throw Exception("@on event nao pode ter parametro(eventName) nullo")
                    val eventEnum = eventName.stringableVal().toEnum<EventFilter>()
                        ?: throw Exception("@on event nao pode ter parametro(eventName) nullo")
                    if (filter != eventEnum) throw Exception("@on event espera: [${filter.name}] mas recebeu [${eventEnum.name}]")

                    fn.children.forEach { ch ->
                        runSingle(filter, ch, context, acc + fn)
                    }
                }
                "@set" -> {
 TODO: FJSKJDKSLFJDSLFDSJLFDKJ
                }
            }
        }.onFailure { err ->
            context.raise(err.message!!)
        }
    }

}