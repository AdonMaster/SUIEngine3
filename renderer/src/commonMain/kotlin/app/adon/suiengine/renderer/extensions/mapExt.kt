package app.adon.suiengine.renderer.extensions

fun <T> MutableMap<String, T>.register(
    vararg names: String,
    evaluator: T
) {
    for (name in names) {
        this[name] = evaluator
    }
}