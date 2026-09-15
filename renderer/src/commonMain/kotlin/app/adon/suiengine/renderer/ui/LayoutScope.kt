package app.adon.suiengine.renderer.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope

sealed interface LayoutScope {
    data class Col(val scope: ColumnScope) : LayoutScope
    data class Row(val scope: RowScope) : LayoutScope
    data class Box(val scope: BoxScope) : LayoutScope

    fun <T> whenBox(body: BoxScope.() -> T): T? {
        return (this as? Box)?.let { body(it.scope) }
    }
    fun <T> whenCol(body: ColumnScope.() -> T): T? {
        return (this as? Col)?.let { body(it.scope) }
    }
    fun <T> whenRow(body: RowScope.() -> T): T? {
        return (this as? Row)?.let { body(it.scope) }
    }
}