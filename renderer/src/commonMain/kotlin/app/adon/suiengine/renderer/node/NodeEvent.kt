package app.adon.suiengine.renderer.node

import app.adon.suiengine.ast.Node
import kotlin.collections.filterIsInstance


fun Node.Fn.childrenFnByName(fnName: String) = this.children
    .filterIsInstance<Node.Fn>()
    .filter { it.name == fnName }

fun Node.Fn.childrenFnByNameAndSingleParamValue(fnName: String, paramValue: String) = this.children
    .filterIsInstance<Node.Fn>()
    .filter { fn ->
        val paramSolver = NodeParamSolver(fn.params, listOf("eventName"))
        fn.name == fnName && paramSolver.get("eventName")?.stringableVal() == paramValue
    }
