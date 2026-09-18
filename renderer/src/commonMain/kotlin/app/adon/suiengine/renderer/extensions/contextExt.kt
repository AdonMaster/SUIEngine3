package app.adon.suiengine.renderer.extensions

import app.adon.suiengine.renderer.Context

fun Context.upwards(cb: (Context)-> Boolean) {
    var current: Context? = this
    while (current != null) {
        if (!cb(current)) break
        current = current.parent
    }
}

inline fun <reified T : Context> Context.closestInstance(predicate: (T)-> Boolean = { true }): T? {
    var current: Context? = this
    while (current != null) {
        if (current is T && predicate(current)) return current
        current = current.parent
    }
    return null
}