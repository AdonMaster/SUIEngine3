package app.adon.suiengine.renderer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.Context
import app.adon.suiengine.renderer.extensions.toColor
import app.adon.suiengine.renderer.node.resolve
import app.adon.suiengine.renderer.node.resolveValFloat
import app.adon.suiengine.renderer.node.resolveValInt
import app.adon.suiengine.renderer.node.resolveValStr
import app.adon.suiengine.renderer.utils.coalesce

@Composable
fun extractModifier(
    params: List<Node.Param>, context: Context, ignoreList: Set<String> = setOf()
): Modifier {
    var mod: Modifier = Modifier
    loop@ for (p in params) {
        mod = runCatching {
            if (ignoreList.contains(p.name)) continue@loop
            when (p.name) {
                "weight" -> {
                    val f = p.value.resolveValFloat(context) ?: throw Exception("weight aceita apenas float")
                    coalesce(
                        context.layoutScope?.whenCol { mod.weight(f) },
                        context.layoutScope?.whenRow { mod.weight(f) },
                        def = mod
                    )
                }
                "align" -> {
                    val alignStr = p.value.resolveValStr(context)
                    coalesce(
                        context.layoutScope?.whenCol {
                            val a = alignStr.toHorizontalAlignment ?: throw Exception("col.align nao aceita: [$alignStr]")
                            mod.align(a)
                        },
                        context.layoutScope?.whenRow {
                            val a = alignStr.toVerticalAlignment ?: throw Exception("row.align nao aceita: [$alignStr]")
                            mod.align(a)
                        },
                        context.layoutScope?.whenBox {
                            val a = alignStr.toAlignment ?: throw Exception("box.align nao aceita: [$alignStr]")
                            mod.align(a)
                        },
                        def = mod
                    )
                }
                "align_by_baseline" -> {
                    context.layoutScope?.whenRow {
                        mod.alignByBaseline()
                    } ?: mod
                }
                "fill" -> {
                    mod.fillMaxSize()
                }
                "w_fill" -> {
                    mod.fillMaxWidth(p.value.resolveValFloat(context) ?: 1f)
                }
                "h_fill" -> {
                    mod.fillMaxHeight(p.value.resolveValFloat(context) ?: 1f)
                }
                "size" -> {
                    val v = p.value.resolveValInt(context) ?: throw Exception("size requires int")
                    mod.size(v.dp)
                }
                "w" -> {
                    val v = p.value.resolveValInt(context) ?: throw Exception("size requires int")
                    mod.width(v.dp)
                }
                "h" -> {
                    val v = p.value.resolveValInt(context) ?: throw Exception("size requires int")
                    mod.height(v.dp)
                }
                "padding" -> {
                    val arr = when (val pvalue = p.value.resolve(context)) {
                        is Node.Arr -> pvalue.v.map { a -> a.stringableVal() }
                        is Node.Integer -> listOf(pvalue.v.toString())
                        is Node.Str -> listOf(pvalue.v)
                        else -> { emptyList() }
                    }
                    when(arr.size) {
                        1 -> mod.padding(arr[0].toDp())
                        2 -> mod.padding(horizontal = arr[0].toDp(), vertical = arr[1].toDp())
                        4 -> mod.padding(arr[3].toDp(), arr[0].toDp(), arr[1].toDp(), arr[2].toDp())
                        else -> mod
                    }
                }
                "background" -> {
                    val clStr = p.value.resolveValStr(context)
                    val cl = clStr.toColor() ?: throw Exception("[$clStr] não parece ser uma cor valida")
                    mod.background(cl)
                }
                else -> { mod }
            }
        }
            .onFailure { context.raise(it.message!!) }
            .getOrDefault(mod)

        // composable outside try catch
        mod = when (p.name) {
            "v_scroll" -> {
                mod.verticalScroll(rememberScrollState())
            }
            "h_scroll" -> {
                mod.horizontalScroll(rememberScrollState())
            }
            else -> mod
        }
    }
    return mod
}