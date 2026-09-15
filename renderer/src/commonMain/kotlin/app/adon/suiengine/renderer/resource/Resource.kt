package app.adon.suiengine.renderer.resource

import org.jetbrains.compose.resources.DrawableResource


@Suppress("ArrayInDataClass")
sealed class ResourceItem {
    data class Drawable(val v: DrawableResource) : ResourceItem()
    data class File(val v: ByteArray) : ResourceItem()
}

class Resource(
) {
    private val _cache = mutableMapOf<String, ResourceItem>()
    val cache: Map<String, ResourceItem> get() = _cache

    fun register(name: String, drawable: DrawableResource): Resource {
        _cache[name] = ResourceItem.Drawable(drawable)
        return this
    }
    fun register(name: String, bytes: ByteArray): Resource {
        _cache[name] = ResourceItem.File(bytes)
        return this
    }

}