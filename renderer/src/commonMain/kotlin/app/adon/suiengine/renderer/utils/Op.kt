package app.adon.suiengine.renderer.utils

inline fun <reified T> Any.takeAs() = this as? T

inline fun <reified T> coalesce(vararg callback: ()->T?): T? {
    for (cb in callback) {
        val vv = cb.invoke()
        if (vv != null) return vv
    }
    return null
}

inline fun <reified T> coalesce(vararg callback: ()->T?, def: ()->T): T {
    return coalesce(*callback) ?: def()
}