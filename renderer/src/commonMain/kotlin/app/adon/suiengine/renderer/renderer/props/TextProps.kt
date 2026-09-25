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
    val paramSolver = paramSolver("text", "size", "style", "text_align", "color", "font_size", "line_height",
        "overflow", "font_weight", "font_style")
    val sText = paramSolver.get("text")?.evalToStr(context) ?: ""
    val styleKey = paramSolver.get("style")?.evalToStr(context)?.toM3StyleKey()
    val textAlign = paramSolver.get("text_align")?.evalToStr(context)?.toTextAlign
    val fontStyle: FontStyle? = if (paramSolver.get("font_style")
            ?.evalToStr(context) == "italic"
    ) FontStyle.Italic else null
    val color =
        paramSolver.get("color")?.evalToStr(context)?.toRGBA() ?: Color.Unspecified
    val fontSize = coalesce(
        { paramSolver.get("font_size")?.eval(context)?.takeAs<Node.Number>()?.v?.sp },
        { paramSolver.get("size")?.eval(context)?.takeAs<Node.Number>()?.v?.sp },
        def = { TextUnit.Unspecified },
    )
    val lineHeight =
        paramSolver.get("line_height")?.eval(context)?.takeAs<Node.Number>()?.v?.sp ?: TextUnit.Unspecified
    val overflow = paramSolver.get("overflow")?.evalToStr(context)?.toTextOverflow
        ?: TextOverflow.Clip
    val weight = paramSolver.get("font_weight")?.evalToStr(context)?.toFontWeight

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