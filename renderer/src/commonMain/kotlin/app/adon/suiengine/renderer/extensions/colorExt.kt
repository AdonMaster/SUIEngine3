package app.adon.suiengine.renderer.extensions

import androidx.compose.ui.graphics.Color

private val namedColors = mapOf(
    "red" to Color.Red,
    "green" to Color.Green,
    "blue" to Color.Blue,
    "black" to Color.Black,
    "white" to Color.White,
    "yellow" to Color.Yellow,
    "cyan" to Color.Cyan,
    "magenta" to Color.Magenta,
    "gray" to Color.Gray,
    "grey" to Color.Gray,
    "transparent" to Color.Transparent
)

fun String.toRGBA(): Color? {
    val cleanInput = this.trim().lowercase()

    namedColors[cleanInput]?.let { return it }

    val cleanHex = cleanInput.removePrefix("#")
    val formattedHex = when (cleanHex.length) {
        6 -> "FF$cleanHex"
        8 -> cleanHex.substring(6, 8) + cleanHex.substring(0, 6)
        else -> return null
    }

    val colorLong = formattedHex.toLongOrNull(16) ?: return null
    return Color(colorLong)
}