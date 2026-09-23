package app.adon.suiengine.ast

import com.benasher44.uuid.uuid4

sealed class NodePathSegment {
    data class Property(val name: String) : NodePathSegment()
    data class Index(val indexNode: Node) : NodePathSegment()

    //
    fun str() = when (this) {
        is Index -> "[${this.indexNode}]"
        is Property -> this.name
    }
}

interface NodeExtended {
    val extension: Node.Fn?
}
sealed class Node {

    val uid: String = uuid4().toString()

    data class Str(val v: String, override val extension: Fn?): Node(), NodeExtended
    data class Number(val v: Double, val hasDigits: Boolean, override val extension: Fn?): Node(), NodeExtended
    data class Bool(val v: Boolean, override val extension: Fn?): Node(), NodeExtended
    data object Null: Node()

    data class Arr(val v: List<Node>, override val extension: Fn?): Node(), NodeExtended
    data class Dict(val v: Map<String, Node>, override val extension: Fn?): Node(), NodeExtended

    data class Fn(
        val name: String, val params: List<Param>, val children: List<Node>,
        override val extension: Fn?
    ): Node(), NodeExtended
    data class Param(val name: String?, val value: Node): Node()
    data class Var(
        val name: String, val segments: List<NodePathSegment>, override val extension: Fn?
    ): Node(), NodeExtended

    fun stringableVal(): String = when (this) {
        is Str -> this.v
        is Number -> {
            if (this.hasDigits) return this.v.toString()
            return this.v.toInt().toString()
        }
        is Bool -> this.v.toString()
        Null -> ""

        is Arr -> v.joinToString(prefix = "[", postfix = "]") { it.stringableVal() }
        is Dict -> v.entries.joinToString(prefix = "{", postfix = "}") { "${it.key}: ${it.value.stringableVal()}" }
        is Param -> "param ($name = ${value.stringableVal()})"
        is Fn -> "fn $name(${params.joinToString { it.stringableVal() }})"
        is Var -> this.name + "." + this.segments.joinToString(".")
    }

}

fun Int.toNode() = Node.Number(this.toDouble(), false, null)
fun Double.toNode() = Node.Number(this, true, null)
fun String.toNode() = Node.Str(this, null)
fun Boolean.toNode() = Node.Bool(this, null)