package app.adon.suiengine.renderer.extensions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

val String.toAlignment: Alignment?
    get() = when (this.trim().lowercase()) {
        "topstart", "top_start", "topleft", "top_left" -> Alignment.TopStart
        "topcenter", "top_center", "top" -> Alignment.TopCenter
        "topend", "top_end", "topright", "top_right" -> Alignment.TopEnd

        "centerstart", "center_start", "centerleft", "center_left" -> Alignment.CenterStart
        "center" -> Alignment.Center
        "centerend", "center_end", "centerright", "center_right" -> Alignment.CenterEnd

        "bottomstart", "bottom_start", "bottomleft", "bottom_left" -> Alignment.BottomStart
        "bottomcenter", "bottom_center", "bottom" -> Alignment.BottomCenter
        "bottomend", "bottom_end", "bottomright", "bottom_right" -> Alignment.BottomEnd

        else -> null
    }


val String.toTextAlign: TextAlign?
    get() = when (this.trim().lowercase()) {
        "left", "start" -> TextAlign.Left
        "right", "end" -> TextAlign.Right
        "center" -> TextAlign.Center
        else -> null
    }


val String.toTextOverflow: TextOverflow?
    get() = when (this.trim().lowercase()) {
        "e_" -> TextOverflow.Ellipsis
        "_e" -> TextOverflow.StartEllipsis
        "_e_" -> TextOverflow.MiddleEllipsis
        else -> null
    }

val String.toFontWeight: FontWeight?
    get() = when (this.trim().lowercase()) {
        "semibold" -> FontWeight.SemiBold
        "bold" -> FontWeight.Bold
        "light" -> FontWeight.Light
        "extralight" -> FontWeight.ExtraLight
        "extrabold" -> FontWeight.ExtraBold
        "thin" -> FontWeight.Thin
        "medium" -> FontWeight.Medium
        else -> null
    }

val String.toHorizontalAlignment: Alignment.Horizontal?
    get() = when (this.trim().lowercase()) {
        "start", "left" -> Alignment.Start
        "center", "centerhorizontally", "center_horizontally" -> Alignment.CenterHorizontally
        "end", "right" -> Alignment.End
        else -> when (this.toAlignment) {
            Alignment.TopStart, Alignment.CenterStart, Alignment.BottomStart -> Alignment.Start
            Alignment.TopCenter, Alignment.Center, Alignment.BottomCenter -> Alignment.CenterHorizontally
            Alignment.TopEnd, Alignment.CenterEnd, Alignment.BottomEnd -> Alignment.End
            else -> null
        }
    }


val String.toVerticalAlignment: Alignment.Vertical?
    get() = when (this.trim().lowercase()) {
        "top" -> Alignment.Top
        "center", "centervertically", "center_vertically" -> Alignment.CenterVertically
        "bottom" -> Alignment.Bottom
        else -> when (this.toAlignment) {
            Alignment.TopStart, Alignment.TopCenter, Alignment.TopEnd -> Alignment.Top
            Alignment.CenterStart, Alignment.Center, Alignment.CenterEnd -> Alignment.CenterVertically
            Alignment.BottomStart, Alignment.BottomCenter, Alignment.BottomEnd -> Alignment.Bottom
            else -> null
        }
    }

val String.toHorizontalArrangement: Arrangement.Horizontal?
    get() {
        val trimmed = this.trim().lowercase()
        return when {
            trimmed.startsWith("spaced_by") || trimmed.startsWith("spaced-by") -> {
                val numStr = trimmed.replace("spaced_by", "").replace("spaced-by", "")
                    .replace("dp", "")
                    .replace(":", "")
                    .trim()
                val value = numStr.toFloatOrNull() ?: 0f
                Arrangement.spacedBy(value.dp)
            }

            trimmed in listOf("start", "left") -> Arrangement.Start
            trimmed == "center" -> Arrangement.Center
            trimmed in listOf("end", "right") -> Arrangement.End
            trimmed in listOf("space_between", "spacebetween", "space-between") -> Arrangement.SpaceBetween
            trimmed in listOf("space_around", "spacearound", "space-around") -> Arrangement.SpaceAround
            trimmed in listOf("space_evenly", "spaceevenly", "space-evenly") -> Arrangement.SpaceEvenly
            else -> null
        }
    }

val String.toVerticalArrangement: Arrangement.Vertical?
    get() {
        val trimmed = this.trim().lowercase()
        return when {
            trimmed.startsWith("spaced_by") || trimmed.startsWith("spaced-by") -> {
                val numStr = trimmed.replace("spaced_by", "").replace("spaced-by", "")
                    .replace("dp", "")
                    .replace(":", "")
                    .trim()
                val value = numStr.toFloatOrNull() ?: 0f
                Arrangement.spacedBy(value.dp)
            }

            trimmed == "top" -> Arrangement.Top
            trimmed == "center" -> Arrangement.Center
            trimmed == "bottom" -> Arrangement.Bottom
            trimmed in listOf("space_between", "spacebetween", "space-between") -> Arrangement.SpaceBetween
            trimmed in listOf("space_around", "spacearound", "space-around") -> Arrangement.SpaceAround
            trimmed in listOf("space_evenly", "spaceevenly", "space-evenly") -> Arrangement.SpaceEvenly
            else -> null
        }
    }