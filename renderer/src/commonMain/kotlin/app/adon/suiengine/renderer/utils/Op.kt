package app.adon.suiengine.renderer.utils

inline fun <reified T> Any.takeAs() = this as? T

data class Triple<A, B, C>(val a: A, val b: B, val c: C)
fun <A, B, C> tripleOf(a: A, b: B, c: C) = Triple(a, b, c)