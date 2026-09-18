package app.adon.suiengine.ast

import com.benasher44.uuid.uuid4

sealed class NodePathSegment {
    data class Property(val name: String) : NodePathSegment()
    data class Index(val indexNode: Node) : NodePathSegment()
}
sealed class Node {

    val uid: String = uuid4().toString()

    data class Str(val v: String): Node()
    data class Integer(val v: Int): Node()
    data class Real(val v: Float): Node()
    data class Bool(val v: Boolean): Node()
    data object Null: Node()

    data class Arr(val v: List<Node>): Node()
    data class Dict(val v: Map<String, Node>): Node()

    data class Fn(val name: String, val params: List<Param>, val children: List<Node>): Node()
    data class Param(val name: String?, val value: Node): Node()
    data class Var(val path: List<NodePathSegment>): Node()

    fun stringableVal(): String = when (this) {
        is Str -> this.v
        is Integer -> this.v.toString()
        is Real -> this.v.toString()
        is Bool -> this.v.toString()
        Null -> ""

        is Arr -> v.joinToString(prefix = "[", postfix = "]") { it.stringableVal() }
        is Dict -> v.entries.joinToString(prefix = "{", postfix = "}") { "${it.key}: ${it.value.stringableVal()}" }
        is Param -> "param ($name = ${value.stringableVal()})"
        is Fn -> "fn $name(${params.joinToString { it.stringableVal() }})"
        is Var -> this.path.joinToString(".")
    }
}