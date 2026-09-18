package app.adon.suiengine.renderer.extensions

import app.adon.suiengine.renderer.Context

fun Context.upwards(cb: (Context)-> Boolean) {
    var current: Context? = this
    while (current != null) {
        if (!cb(current)) break
        current = current.parent
    }
}

inline fun <reified T : Context> Context.closestInstance(): T? {
    var current: Context? = this
    while (current != null) {
        if (current is T) return current
        current = current.parent
    }
    return null
}