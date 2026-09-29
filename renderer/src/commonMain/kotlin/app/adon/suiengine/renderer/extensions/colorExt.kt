package app.adon.suiengine.renderer.extensions

import androidx.compose.ui.graphics.Color

private val namedColors = mapOf(
    "red" to Color(0xFFFF0000),
    "green" to Color(0xFF008000),
    "lime" to Color(0xFF00FF00),
    "blue" to Color(0xFF0000FF),
    "yellow" to Color(0xFFFFEB3B),
    "cyan" to Color(0xFF00FFFF),
    "magenta" to Color(0xFFFF00FF),
    "orange" to Color(0xFFFFA500),
    "purple" to Color(0xFF800080),
    "pink" to Color(0xFFFFC0CB),
    "brown" to Color(0xFFA52A2A),

    // Neutras e Escalas
    "black" to Color(0xFF000000),
    "white" to Color(0xFFFFFFFF),
    "gray" to Color(0xFF808080),
    "grey" to Color(0xFF808080),
    "lightgray" to Color(0xFFD3D3D3),
    "lightgrey" to Color(0xFFD3D3D3),
    "darkgray" to Color(0xFFA9A9A9),
    "darkgrey" to Color(0xFFA9A9A9),
    "transparent" to Color(0x00000000),

    // Tons Adicionais Frequentes (CSS Extendido)
    "teal" to Color(0xFF008080),
    "navy" to Color(0xFF000080),
    "indigo" to Color(0xFF4B0082),
    "violet" to Color(0xFFEE82EE),
    "gold" to Color(0xFFFFD700),
    "coral" to Color(0xFFFF7F50),
    "turquoise" to Color(0xFF40E0D0)
)

// Cores base do Material (Tom 500)
private val materialBases = mapOf(
    "red" to Color(0xFFF44336),
    "pink" to Color(0xFFE91E63),
    "purple" to Color(0xFF9C27B0),
    "deep_purple" to Color(0xFF673AB7),
    "indigo" to Color(0xFF3F51B5),
    "blue" to Color(0xFF2196F3),
    "light_blue" to Color(0xFF03A9F4),
    "cyan" to Color(0xFF00BCD4),
    "teal" to Color(0xFF009688),
    "green" to Color(0xFF4CAF50),
    "light_green" to Color(0xFF8BC34A),
    "lime" to Color(0xFFCDDC39),
    "yellow" to Color(0xFFFFEB3B),
    "amber" to Color(0xFFFFC107),
    "orange" to Color(0xFFFF9800),
    "deep_orange" to Color(0xFFFF5722),
    "brown" to Color(0xFF795548),
    "grey" to Color(0xFF9E9E9E),
    "gray" to Color(0xFF9E9E9E),
    "blue_grey" to Color(0xFF607D8B)
)

fun String.toRGBA(): Color? {
    val cleanInput = this.trim().lowercase()

    // 1. Busca por nome simples (ex: "orange", "transparent", "red")
    namedColors[cleanInput]?.let { return it }

    // 2. Tenta parsear sufixo de tom Material (ex: "red_100", "blue_700", "deep_orange_200")
    if (cleanInput.contains("_")) {
        val lastUnderscore = cleanInput.lastIndexOf('_')
        val baseName = cleanInput.substring(0, lastUnderscore)
        val weight = cleanInput.substring(lastUnderscore + 1).toIntOrNull()

        if (weight != null) {
            val baseColor = materialBases[baseName] ?: namedColors[baseName]
            if (baseColor != null) {
                return applyMaterialWeight(baseColor, weight)
            }
        }
    }

    // 3. Fallback Hexadecimal (#RRGGBB ou #AARRGGBB)
    val cleanHex = cleanInput.removePrefix("#")
    val formattedHex = when (cleanHex.length) {
        6 -> "FF$cleanHex"
        8 -> cleanHex.substring(6, 8) + cleanHex.substring(0, 6)
        else -> return null
    }

    val colorLong = formattedHex.toLongOrNull(16) ?: return null
    return Color(colorLong)
}

private fun applyMaterialWeight(baseColor: Color, weight: Int): Color {
    if (weight == 500) return baseColor
    val factor = (500 - weight) / 500f
    return if (factor > 0) {
        blendColor(baseColor, Color.White, factor * 0.82f)
    } else {
        blendColor(baseColor, Color.Black, -factor * 0.72f)
    }
}

private fun blendColor(color1: Color, color2: Color, ratio: Float): Color {
    val inverse = 1f - ratio
    return Color(
        red = color1.red * inverse + color2.red * ratio,
        green = color1.green * inverse + color2.green * ratio,
        blue = color1.blue * inverse + color2.blue * ratio,
        alpha = color1.alpha
    )
}