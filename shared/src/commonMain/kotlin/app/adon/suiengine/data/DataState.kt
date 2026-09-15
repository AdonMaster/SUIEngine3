package app.adon.suiengine.data

sealed interface DataState<out T> {
    data object Idle: DataState<Nothing>
    data object Loading: DataState<Nothing>
    data class Error(val msg: String): DataState<Nothing>
    data class Success<T>(val payload: T): DataState<T>
}