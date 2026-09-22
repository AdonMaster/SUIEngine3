package app.adon.suiengine.renderer.node

import app.adon.suiengine.ast.Node

fun Node.attachExtension(newExt: Node.Fn?): Node = when(this) {
    is Node.Str -> copy(extension = newExt)
    is Node.Number -> copy(extension = newExt)
    is Node.Bool -> copy(extension = newExt)
    is Node.Arr -> copy(extension = newExt)
    is Node.Dict -> copy(extension = newExt)
    is Node.Var -> copy(extension = newExt)
    is Node.Fn -> copy(extension = newExt)
    is Node.Param, Node.Null -> this
}