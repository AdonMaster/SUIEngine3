package app.adon.suiengine.renderer.node

import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.Context
import app.adon.suiengine.renderer.extensions.register

val evalRegistryLayout = buildMap {
    register("is_dark") { fn: Node.Fn, context: Context, seen: MutableSet<String> ->
        val hexColorNode = fn.params.firstOrNull()?.value?.evalAs<Node.Str>(context, seen)
            ?: throw RuntimeException("is_dark requer uma string de cor hex (ex: '0F172A')")

        val hex = hexColorNode.v.removePrefix("#")
        val colorInt = hex.toLongOrNull(16) ?: 0L

        // Extrai RGB e calcula luminância percebida simples
        val r = (colorInt shr 16 and 0xFF)
        val g = (colorInt shr 8 and 0xFF)
        val b = (colorInt and 0xFF)
        val luminance = (0.299 * r + 0.587 * g + 0.114 * b)

        Node.Bool(luminance < 128)
    }
}