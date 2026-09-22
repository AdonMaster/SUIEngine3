package app.adon.suiengine.renderer.extensions

import app.adon.suiengine.ast.Node

inline fun <reified O> Node.assert(err: String): O {
    return this as? O ?: throw RuntimeException(err)
}

fun Node.assertNum(field: String) = this.assert<Node.Number>("$field só aceita numeros")
fun Node.Arr.assertNumArr(field: String) = v.map {
    it as? Node.Number ?: throw Exception("$field é um vetor de numeros")
}