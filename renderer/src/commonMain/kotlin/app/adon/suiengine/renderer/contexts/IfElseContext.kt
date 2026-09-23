package app.adon.suiengine.renderer.contexts

import app.adon.suiengine.renderer.SUIEngineVM

enum class IfElseType { IF, ELSE, ELSE_IF }
sealed interface IfElseStatus {
    object AlreadyResolved: IfElseStatus
    object ElseForceResolve: IfElseStatus
    data class NotResolved(val extracted: Boolean): IfElseStatus
}
class IfElseContext(
    val type: IfElseType, val resolved: Boolean,
    parent: Context?, vm: SUIEngineVM
): Context("if_else", parent, vm)