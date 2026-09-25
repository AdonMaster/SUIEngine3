package app.adon.suiengine.ast

fun String.toVarNode(extension: Node.Fn? = null): Node.Var {
    val fullPath = this

    // 1. Validações básicas de formato geral
    if (fullPath.isBlank() || fullPath.startsWith(".") || fullPath.endsWith(".") || fullPath.contains("..")) {
        throw RuntimeException("Sintaxe de variável inválida: '$fullPath'")
    }

    // Regex para validar identificador com [índice] opcional: ex "orders[0]"
    val tokenRegex = """^([a-zA-Z_][a-zA-Z0-9_]*)(?:\[([^\]]+)\])?$""".toRegex()

    val parts = fullPath.split(".")
    val rootName = parts.first()

    // Valida a raiz
    val rootMatch = tokenRegex.matchEntire(rootName)
        ?: throw RuntimeException("Nome de variável raiz inválido: '$rootName'")

    val segments = mutableListOf<NodePathSegment>()

    // Se a raiz teve índice (ex: "users[0]"), o [0] vira primeiro segmento
    rootMatch.groupValues[2].takeIf { it.isNotEmpty() }?.let { rawIndex ->
        val indexInt = rawIndex.toIntOrNull()
            ?: throw RuntimeException("Índice de array inválido em '$rootName': '$rawIndex' não é um inteiro")
        segments.add(NodePathSegment.Index(indexInt.toNode()))
    }

    // Processa os acessores subsequentes (se existirem)
    for (part in parts.drop(1)) {
        val match = tokenRegex.matchEntire(part)
            ?: throw RuntimeException("Segmento inválido: '$part'")

        val propName = match.groupValues[1]
        val rawIndex = match.groupValues[2]

        // Adiciona a propriedade
        segments.add(NodePathSegment.Property(propName))

        // Se houver [index], valida se é inteiro válido
        if (rawIndex.isNotEmpty()) {
            val indexInt = rawIndex.toIntOrNull()
                ?: throw RuntimeException("Índice de array inválido em '$part': '$rawIndex' não é um inteiro válido")

            segments.add(NodePathSegment.Index(indexInt.toNode()))
        }
    }

    return Node.Var(
        name = rootMatch.groupValues[1],
        segments = segments, // Pode ser lista vazia normalmente!
        extension = extension
    )
}