package app.adon.suiengine.renderer.utils

fun <T> coalesce(vararg items: T?, def: T): T {
    for (i in items) {
        if (i != null) return i
    }
    return def
}