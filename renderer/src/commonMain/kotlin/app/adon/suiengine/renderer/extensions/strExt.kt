package app.adon.suiengine.renderer.extensions

inline fun <reified T: Enum<T>> String.toEnum(): T? {
    val rr = enumValues<T>().firstOrNull() {
        it.name.equals(this.trim(), ignoreCase = true)
    }
    return rr
}
