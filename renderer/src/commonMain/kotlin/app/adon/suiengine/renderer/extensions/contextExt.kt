package app.adon.suiengine.renderer.extensions

import app.adon.suiengine.renderer.Context

fun Context.upwards(cb: (Context)-> Boolean) {
    var current: Context? = this
    while (current != null) {
        if (!cb(current)) break
        current = current.parent
    }
}