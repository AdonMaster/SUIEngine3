package app.adon.suiengine.renderer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.events.EventFilter
import app.adon.suiengine.renderer.events.EventRegistry
import app.adon.suiengine.renderer.extensions.toColor
import app.adon.suiengine.renderer.node.NodeParamSolver
import app.adon.suiengine.renderer.node.eval
import app.adon.suiengine.renderer.node.evalAsInt
import app.adon.suiengine.renderer.node.evalAsStrValue
import app.adon.suiengine.renderer.ui.extractModifier
import app.adon.suiengine.renderer.ui.toAlignment
import app.adon.suiengine.renderer.ui.toFontWeight
import app.adon.suiengine.renderer.ui.toHorizontalAlignment
import app.adon.suiengine.renderer.ui.toHorizontalArrangement
import app.adon.suiengine.renderer.ui.toM3Style
import app.adon.suiengine.renderer.ui.toTextAlign
import app.adon.suiengine.renderer.ui.toTextOverflow
import app.adon.suiengine.renderer.ui.toVerticalAlignment
import app.adon.suiengine.renderer.ui.toVerticalArrangement


typealias FnRenderer = @Composable (Node.Fn, Context) -> Unit
object FunctionRegistry {

    @Composable
    fun InvokeGroup(nodes: List<Node>, context: Context) {
        for (node in nodes.filterIsInstance<Node.Fn>()) {
            val fn = renderers[node.name]
            if (fn != null) {
                fn.invoke(node, context)
            } else {
                context.raise("Função não encontrada [${node.name}]")
            }
        }
    }

    private val renderers = mapOf<String, FnRenderer>(

        "@state" to { node, context ->
            node.params.forEach { param ->
                context.initialState(node.uid, param.name, param.value)
            }
        },

        "@on" to { node, context ->
            //ignored @see EventRegistry
        },

        "@if" to { node, context ->
            InvokeGroup(node.children, context.newChild(node.name))
        },

        "@set" to { node, context ->

        },

        "box" to { node, context ->
            val mod = extractModifier(node.params, context)
            val paramSolver = NodeParamSolver(node.params, listOf("content_align"))
            val contentAlign = paramSolver.get("content_align")
                ?.eval(context)?.stringableVal()?.toAlignment
                ?: Alignment.TopStart
            Box(modifier = mod, contentAlignment = contentAlign) {
                InvokeGroup(
                    nodes = node.children,
                    context = context.newChild(node.name).withLayoutScope(this)
                )
            }
        },

        "col" to { node, context ->
            val mod = extractModifier(node.params, context)
            val paramSolver = NodeParamSolver(node.params, listOf("vertical_arrangement", "horizontal_alignment"))
            val vArrangement = paramSolver.get("vertical_arrangement")?.evalAsStrValue(context)?.toVerticalArrangement ?: Arrangement.Top
            val hAlign = paramSolver.get("horizontal_alignment")?.evalAsStrValue(context)?.toHorizontalAlignment ?: Alignment.Start
            Column(
                modifier = mod,
                verticalArrangement = vArrangement,
                horizontalAlignment = hAlign
            ) {
                InvokeGroup(
                    nodes = node.children,
                    context = context.newChild(node.name).withLayoutScope(this)
                )
            }
        },

        "row" to { node, context ->
            val paramSolver = NodeParamSolver(node.params, listOf("horizontal_arrangement", "vertical_alignment"))
            val hArrangement = paramSolver.get("horizontal_arrangement")?.evalAsStrValue(context)?.toHorizontalArrangement ?: Arrangement.Start
            val vAlign = paramSolver.get("vertical_alignment")?.evalAsStrValue(context)?.toVerticalAlignment ?: Alignment.Top
            val mod = extractModifier(node.params, context)
            Row(
                modifier = mod,
                horizontalArrangement = hArrangement,
                verticalAlignment = vAlign
            ) {
                InvokeGroup(
                    nodes = node.children,
                    context = context.newChild(node.name).withLayoutScope(this)
                )
            }
        },

        "spacer" to { node, context ->
            Spacer(modifier = extractModifier(node.params, context))
        },

        "divider" to { node, context ->
            HorizontalDivider(modifier = Modifier)
        },

        "text" to { node, context ->
            // params
            val paramSolver = NodeParamSolver(node.params, listOf(
                "text", "style", "text_align", "color", "size", "line_height", "overflow", "weight", "font_style"
            ))
            val sText = paramSolver.get("text")?.evalAsStrValue(context) ?: ""
            val style: TextStyle = paramSolver.get("style")?.evalAsStrValue(context)?.toM3Style ?: LocalTextStyle.current
            val textAlign = paramSolver.get("text_align")?.evalAsStrValue(context)?.toTextAlign
            val fontStyle: FontStyle? = if (paramSolver.get("font_style")?.evalAsStrValue(context) == "italic") FontStyle.Italic else null
            val color = paramSolver.get("color")?.evalAsStrValue(context)?.toColor() ?: Color.Unspecified
            val size = paramSolver.get("size")?.evalAsInt(context)?.v?.sp ?: TextUnit.Unspecified
            val lineHeight = paramSolver.get("line_height")?.evalAsInt(context)?.v?.sp ?: TextUnit.Unspecified
            val overflow = paramSolver.get("overflow")?.evalAsStrValue(context)?.toTextOverflow ?: TextOverflow.Clip
            val weight = paramSolver.get("weight")?.evalAsStrValue(context)?.toFontWeight

            //
            val mod = extractModifier(node.params, context)
            Text(
                text = sText, modifier = mod, fontStyle = fontStyle, textAlign = textAlign,
                color = color, fontSize = size, lineHeight = lineHeight, overflow = overflow,
                fontWeight = weight, style = style
            )
        },

        "btn" to { node, context ->
            val paramSolver = NodeParamSolver(node.params, listOf("text"))
            val textValue = paramSolver.get("text")?.eval(context)?.stringableVal()
            Button(
                modifier = extractModifier(node.params, context),
                onClick = {
                    EventRegistry.trigger(EventFilter.TOUCH, node, context)
                }
            ) {
                if (textValue != null) {
                    Text(textValue)
                } else {
                    InvokeGroup(node.children, context.newChild(node.name))
                }
            }
        }
    )

}