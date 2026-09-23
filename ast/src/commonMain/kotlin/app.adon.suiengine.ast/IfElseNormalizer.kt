package app.adon.suiengine.ast

private val IF_KEYWORDS = setOf("if_chain", "if", "else_if", "else")

fun List<Node>.normalizeIfChains(): List<Node> {
    val result = mutableListOf<Node>()
    var currentChain: MutableList<Node.Fn>? = null

    fun flushChain() {
        currentChain?.let { chain ->
            result.add(
                Node.Fn(
                    name = "if_chain",
                    params = emptyList(),
                    children = chain,
                    extension = null
                )
            )
        }
        currentChain = null
    }

    for (rawItem in this) {
        val item = if (rawItem is Node.Fn) {
            rawItem.copy(children = rawItem.children.normalizeIfChains())
        } else {
            rawItem
        }

        if (item is Node.Fn && item.name in IF_KEYWORDS) {
            when (item.name) {
                "if_chain" -> {
                    throw RuntimeException("[if_chain] is reserved")
                }
                "if" -> {
                    item.assertOneParam("if")
                    flushChain()
                    currentChain = mutableListOf(item)
                }
                "else_if", "else" -> {
                    if (item.name == "else_if") {
                        item.assertOneParam(item.name)
                    } else {
                        if (item.params.isNotEmpty()) item.assertNoParams(item.name)
                    }
                    if (currentChain != null) {
                        currentChain!!.add(item)
                    } else {
                        throw RuntimeException("else/else_if orfão de [if]")
                    }
                }
            }
        } else {
            flushChain()
            result.add(item)
        }
    }

    flushChain()
    return result
}

private fun Node.Fn.assertOneParam(name: String) {
    if (params.isEmpty()) throw RuntimeException("[$name] requires a parameter")
    if (params.size > 1)  throw RuntimeException("[$name] supports only 1 parameter")
}

private fun Node.Fn.assertNoParams(name: String) {
    if (params.isNotEmpty()) throw RuntimeException("[$name] doesn't support parameter")
}