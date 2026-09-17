package app.adon.suiengine.renderer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import app.adon.suiengine.renderer.extensions.register
import app.adon.suiengine.renderer.extensions.toColor
import app.adon.suiengine.renderer.node.InvokeRegistry
import app.adon.suiengine.renderer.node.NodeEvaluatorFnNotFound
import app.adon.suiengine.renderer.node.NodeParamSolver
import app.adon.suiengine.renderer.node.paramSolver
import app.adon.suiengine.renderer.node.resolve
import app.adon.suiengine.renderer.node.resolveValBool
import app.adon.suiengine.renderer.node.resolveValInt
import app.adon.suiengine.renderer.node.resolveValStr
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
import app.adon.suiengine.renderer.utils.coalesce


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

    private val renderers = buildMap<String, FnRenderer> {

        register("@state", "@declare") { node: Node.Fn, context: Context ->
            node.params.forEach { param ->
                context.initialState(node.uid, param.name, param.value)
            }
        }

        register("@render") { callerNode: Node.Fn, context: Context ->
            val componentNode = callerNode.paramSolver("component").get("component")
            if (componentNode == null) {
                context.raise("@render requires a component as param")
            } else {
                val fnNode = try {
                     componentNode.resolve(context)
                } catch (e: NodeEvaluatorFnNotFound) {
                    e.fn
                }
                if (fnNode !is Node.Fn) {
                    context.raise("@render must resolve to a function [${fnNode.stringableVal()}]")
                } else {
                    val renderContext = context.newChild("render")
                    callerNode.params.filter { it.name != "component" && it.name != null }.forEach { param ->
                        renderContext.setVirtual(param.name!!, param.value)
                    }
                    InvokeGroup(listOf(fnNode), renderContext)
                }
            }
        }

        register("@if") { node: Node.Fn, context: Context ->
            val render = runCatching {
                node.paramSolver("val").get("val")?.resolveValBool(context)
                    ?: throw Exception("@if param == bool")
            }.onFailure {
                context.raise(it.message!!)
            }.getOrDefault(false)

            if (render) {
                InvokeGroup(node.children, context.newChild(node.name))
            }
        }
        register("@foreach") { node: Node.Fn, context: Context ->
            val paramSolver = node.paramSolver("items", "as")
            val arrayNode = paramSolver.get("items")?.resolve(context) as? Node.Arr

            if (arrayNode != null) {
                val asName = paramSolver.get("as")?.resolveValStr(context) ?: "it"
                arrayNode.v.forEachIndexed { index, itemValue ->
                    val loopContext = context.newChild(node.name)
                    loopContext.setVirtual(asName, itemValue)
                    loopContext.setVirtual("index", Node.Integer(index))
                    loopContext.setLayoutScope(context.layoutScope)
                    InvokeGroup(node.children, loopContext)
                }
            } else {
                context.raise("@foreach requires an array parameter")
            }
        }

        register("box") { node: Node.Fn, context: Context ->
            val mod = extractModifier(node.params, context)
            val paramSolver = NodeParamSolver(node.params, listOf("content_align"))
            val contentAlign = paramSolver.get("content_align")
                ?.resolveValStr(context)?.toAlignment
                ?: Alignment.TopStart
            Box(modifier = mod, contentAlignment = contentAlign) {
                InvokeGroup(
                    nodes = node.children,
                    context = context.newChild(node.name).withLayoutScope(this)
                )
            }
        }

        register("col") { node: Node.Fn, context: Context ->
            val mod = extractModifier(node.params, context)
            val paramSolver = NodeParamSolver(node.params, listOf("v_arrange", "h_align"))
            val vArrangement =
                paramSolver.get("v_arrange")?.resolveValStr(context)?.toVerticalArrangement
                    ?: Arrangement.Top
            val hAlign = paramSolver.get("h_align")?.resolveValStr(context)?.toHorizontalAlignment
                ?: Alignment.Start

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
        }

        register("lazy_col") { node: Node.Fn, context: Context ->
            val paramSolver = node.paramSolver("items", "as", "v_arrange", "h_align")
            val array = paramSolver.get("items")?.resolve(context) as? Node.Arr
            if (array == null) {
                context.raise("lazy_col requires 'items' param of type array")
            } else {
                val vArrangement =
                    paramSolver.get("v_arrange")?.resolveValStr(context)?.toVerticalArrangement
                        ?: Arrangement.Top
                val hAlign =
                    paramSolver.get("h_align")?.resolveValStr(context)?.toHorizontalAlignment
                        ?: Alignment.Start
                val asName = paramSolver.get("as")?.resolveValStr(context) ?: "it"
                LazyColumn(
                    modifier = extractModifier(node.params, context),
                    verticalArrangement = vArrangement,
                    horizontalAlignment = hAlign
                ) {
                    items(array.v.size, key = { array.v[it].stringableVal() }) { index ->
                        val rowContext = context.newChild(node.name)
                        rowContext.setVirtual(asName, array.v[index])
                        rowContext.setVirtual("index", Node.Integer(index))
                        InvokeGroup(node.children, rowContext)
                    }
                }
            }
        }

        register("row") { node: Node.Fn, context: Context ->
            val paramSolver = NodeParamSolver(node.params, listOf("h_arrange", "h_align"))
            val hArrangement =
                paramSolver.get("h_arrange")?.resolveValStr(context)?.toHorizontalArrangement
                    ?: Arrangement.Start
            val vAlign = paramSolver.get("h_align")?.resolveValStr(context)?.toVerticalAlignment
                ?: Alignment.Top
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
        }

        register("spacer") { node: Node.Fn, context: Context ->
            Spacer(modifier = extractModifier(node.params, context))
        }

        register("divider") { node: Node.Fn, context: Context ->
            HorizontalDivider(modifier = Modifier)
        }

        register("text") { node: Node.Fn, context: Context ->
            // params
            val paramSolver = NodeParamSolver(
                node.params, listOf(
                    "text", "size", "style", "text_align", "color", "font_size", "line_height",
                    "overflow", "font_weight", "font_style"
                )
            )
            val sText = paramSolver.get("text")?.resolveValStr(context) ?: ""
            val style: TextStyle = paramSolver.get("style")?.resolveValStr(context)?.toM3Style
                ?: LocalTextStyle.current
            val textAlign = paramSolver.get("text_align")?.resolveValStr(context)?.toTextAlign
            val fontStyle: FontStyle? = if (paramSolver.get("font_style")
                    ?.resolveValStr(context) == "italic"
            ) FontStyle.Italic else null
            val color =
                paramSolver.get("color")?.resolveValStr(context)?.toColor() ?: Color.Unspecified
            val fontSize = coalesce(
                paramSolver.get("font_size")?.resolveValStr(context)?.toFloatOrNull()?.sp,
                paramSolver.get("size")?.resolveValStr(context)?.toFloatOrNull()?.sp,
                def = TextUnit.Unspecified,
            )
            val lineHeight =
                paramSolver.get("line_height")?.resolveValInt(context)?.sp ?: TextUnit.Unspecified
            val overflow = paramSolver.get("overflow")?.resolveValStr(context)?.toTextOverflow
                ?: TextOverflow.Clip
            val weight = paramSolver.get("font_weight")?.resolveValStr(context)?.toFontWeight

            //
            val mod = extractModifier(node.params, context, ignoreList = setOf("size"))
            Text(
                text = sText, modifier = mod, fontStyle = fontStyle, textAlign = textAlign,
                color = color, fontSize = fontSize, lineHeight = lineHeight, overflow = overflow,
                fontWeight = weight, style = style
            )
        }

        register("btn") { node: Node.Fn, context: Context ->
            val paramSolver = NodeParamSolver(node.params, listOf("text", "on_touch"))
            val textValue = paramSolver.get("text")?.resolveValStr(context)
            val onTouch = paramSolver.get("on_touch")
            Button(
                modifier = extractModifier(node.params, context),
                onClick = { InvokeRegistry.trigger(onTouch, context) }
            ) {
                if (textValue != null) {
                    Text(textValue)
                }
                InvokeGroup(node.children, context.newChild(node.name))
            }
        }

        register("surface") { node: Node.Fn, context: Context ->
            var mod = extractModifier(node.params, context, ignoreList = setOf("background"))
            val paramSolver = node.paramSolver(
                "corner_radius", "background", "foreground", "on_touch",
                "elevation", "clickable"
            )
            val cornerRadius = paramSolver.get("corner_radius")?.resolveValInt(context) ?: 8
            val shape = RoundedCornerShape(cornerRadius.dp)
            val foreground = paramSolver.get("foreground")?.resolveValStr(context)?.toColor()
                ?: MaterialTheme.colorScheme.onSurface
            val background = paramSolver.get("background")?.resolveValStr(context)?.toColor()
                ?: MaterialTheme.colorScheme.surface
            val elevation = paramSolver.get("elevation")?.resolveValInt(context) ?: 0
            val onTouch = paramSolver.get("on_touch")

            // clickable
            mod = mod.clickable(onTouch != null) {
                InvokeRegistry.trigger(onTouch, context)
            }

            Surface(
                modifier = mod,
                shape = shape,
                contentColor = foreground,
                color = background,
                shadowElevation = elevation.dp
            ) {
                InvokeGroup(node.children, context.newChild(node.name))
            }
        }

        register("textfield") { node: Node.Fn, context: Context ->
            val textState = rememberTextFieldState(initialText = "")
            var mod = extractModifier(node.params, context, ignoreList = setOf())

            val paramSolver = node.paramSolver("label", "ph")
            val label = paramSolver.get("label")?.resolveValStr(context)
            val ph = paramSolver.get("ph")?.resolveValStr(context)

            OutlinedTextField(
                modifier = mod,
                state = textState,
                placeholder = ph?.let { { Text(ph) } },
                label = label?.let {{ Text(label) }}
            )
        }
    }

}