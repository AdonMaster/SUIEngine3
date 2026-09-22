package app.adon.suiengine.renderer.node.modifier

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.adon.suiengine.ast.Node
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.assertNum
import app.adon.suiengine.renderer.extensions.assertNumArr
import app.adon.suiengine.renderer.extensions.toRGBA
import app.adon.suiengine.renderer.node.eval.eval


fun Node.Fn.extractModifier(context: Context, ignore: List<String> = emptyList()) = params
    .filter { it.name != null && !ignore.contains(it.name) }
    .fold(initial = Modifier) { acc: Modifier, p ->
        val vv = p.value
        when (p.name!!) {
            "h", "height" -> acc.height(vv.eval(context).assertNum("height").v.toFloat().dp)
            "w", "width" -> acc.width(vv.eval(context).assertNum("width").v.toFloat().dp)
            "size" -> acc.size(vv.eval(context).assertNum("size").v.toFloat().dp)
            "fill" -> acc.fillMaxSize()
            "w_fill" -> {
                acc.fillMaxWidth(vv.eval(context).assertNum("w_fill").v.toFloat())
            }
            "h_fill" -> {
                acc.fillMaxHeight(vv.eval(context).assertNum("h_fill").v.toFloat())
            }
            "background" -> {
                val ss = vv.eval(context).stringableVal()
                acc.background(ss.toRGBA()
                    ?: throw RuntimeException("$ss não parece ser uma cor válida"))
            }
            "padding" -> {
                when (val res = vv.eval(context)) {
                    is Node.Number -> acc.padding(res.v.dp)
                    is Node.Arr -> {
                        val numArr = res.assertNumArr("padding")
                        when (numArr.size) {
                            2 -> acc.padding(vertical = numArr[0].v.dp, horizontal = numArr[1].v.dp)
                            4 -> acc.padding(numArr[3].v.dp, numArr[0].v.dp, numArr[1].v.dp, numArr[2].v.dp)
                            else -> throw RuntimeException("padding com [formato] inválido")
                        }
                    }
                    else -> throw RuntimeException("padding com [valor] inválido")
                }
            }
            "circle" -> acc.clip(CircleShape)
            "rounded", "corner" -> {
                val r = vv.eval(context).assertNum(p.name!!).v.toFloat()
                acc.clip(RoundedCornerShape(r.dp))
            }
            else -> acc
        }
    }



