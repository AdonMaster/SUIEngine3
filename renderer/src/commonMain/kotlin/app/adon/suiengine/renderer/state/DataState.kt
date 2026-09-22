package app.adon.suiengine.renderer.state

sealed interface DataState<out T> {
    data object Idle: DataState<Nothing>
    data object Loading: DataState<Nothing>
    data class Success<T>(val payload: T): DataState<T>
    data class Error(val reason: String): DataState<Nothing>
}