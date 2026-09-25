package app.adon.suiengine.renderer.layout

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope

sealed interface LayoutScope {
    object None: LayoutScope
    data class Box(val v: BoxScope): LayoutScope
    data class Col(val v: ColumnScope): LayoutScope
    data class Row(val v: RowScope): LayoutScope
}

fun BoxScope.toLayoutScope() = LayoutScope.Box(this)
fun RowScope.toLayoutScope() = LayoutScope.Row(this)
fun ColumnScope.toLayoutScope() = LayoutScope.Col(this)