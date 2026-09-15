package app.adon.suiengine.renderer.extensions

import androidx.compose.ui.graphics.Color

inline fun <reified T: Enum<T>> String.toEnum(): T? {
    val rr = enumValues<T>().firstOrNull() {
        it.name.equals(this.trim(), ignoreCase = true)
    }
    return rr
}

fun String.toColor(): Color? {
    val cleanHex = this.removePrefix("#")
    val formattedHex = when (cleanHex.length) {
        6 -> "FF$cleanHex"
        8 -> cleanHex.substring(6, 8) + cleanHex.substring(0, 6)
        else -> return null
    }
    val colorLong = formattedHex.toLongOrNull(16) ?: return null
    return Color(colorLong)
}