package app.adon.suiengine.renderer.renderer.props

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.M3TextStyleKey
import app.adon.suiengine.renderer.extensions.toFontWeight
import app.adon.suiengine.renderer.extensions.toM3StyleKey
import app.adon.suiengine.renderer.extensions.toRGBA
import app.adon.suiengine.renderer.extensions.toTextAlign
import app.adon.suiengine.renderer.extensions.toTextOverflow
import app.adon.suiengine.renderer.node.eval.eval
import app.adon.suiengine.renderer.node.eval.evalToStr
import app.adon.suiengine.renderer.node.modifier.extractModifier
import app.adon.suiengine.renderer.node.paramSolver
import app.adon.suiengine.renderer.utils.coalesce
import app.adon.suiengine.renderer.utils.takeAs
import app.adon.suiengine.renderer.utils.ter

data class TextProps(
    val modifier: Modifier,
    val text: String,
    val styleKey: M3TextStyleKey?,
    val textAlign: TextAlign?,
    val fontStyle: FontStyle?,
    val color: Color,
    val fontSize: TextUnit,
    val lineHeight: TextUnit,
    val overflow: TextOverflow,
    val weight: FontWeight?,
)

fun Node.Fn.resolveTextProps(context: Context): TextProps {
    val ps = paramSolver(
        "text", "size", "style", "text_align", "color", "font_size", "line_height", "font_style",
        "overflow", "italic", "bold", "semibold", "light", "extralight", "extrabold", "thin", "medium",
        "font_weight"
    )
    val sText = ps.get("text")?.evalToStr(context) ?: ""
    val styleKey = ps.get("style")?.evalToStr(context)?.toM3StyleKey()
    val textAlign = ps.get("text_align")?.evalToStr(context)?.toTextAlign
    val fontStyle: FontStyle? = coalesce( {
        ter(ps.has("italic"), FontStyle.Italic, null)
    }, {
        ter(ps.get("font_style")?.evalToStr(context) == "italic", FontStyle.Italic, null)
    })

    val color =
        ps.get("color")?.evalToStr(context)?.toRGBA() ?: Color.Unspecified
    val fontSize = coalesce(
        { ps.get("font_size")?.eval(context)?.takeAs<Node.Number>()?.v?.sp },
        { ps.get("size")?.eval(context)?.takeAs<Node.Number>()?.v?.sp },
        def = { TextUnit.Unspecified },
    )
    val lineHeight =
        ps.get("line_height")?.eval(context)?.takeAs<Node.Number>()?.v?.sp ?: TextUnit.Unspecified
    val overflow = ps.get("overflow")?.evalToStr(context)?.toTextOverflow
        ?: TextOverflow.Clip
    val weight: FontWeight? = coalesce({
        when {
            ps.has("bold") -> FontWeight.Bold
            ps.has("semibold") -> FontWeight.SemiBold
            ps.has("light") -> FontWeight.Light
            ps.has("extralight") -> FontWeight.ExtraLight
            ps.has("extrabold") -> FontWeight.ExtraBold
            ps.has("thin") -> FontWeight.Thin
            ps.has("medium") -> FontWeight.Medium
            else -> null
        }
    }, {
        ps.get("font_weight")?.evalToStr(context)?.toFontWeight
    })

    return TextProps(
        modifier = extractModifier(context, listOf("size")),
        text = sText,
        styleKey = styleKey,
        textAlign = textAlign,
        fontStyle = fontStyle,
        color = color,
        fontSize = fontSize,
        lineHeight = lineHeight,
        overflow = overflow,
        weight = weight
    )
}