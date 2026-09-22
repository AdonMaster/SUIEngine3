package app.adon.suiengine.ast

class Parser(private val tokens: List<Token>) {

    private var current = 0

    private fun skipComments() {
        while (current < tokens.size &&
            (tokens[current].type == TokenType.COMMENT || tokens[current].type == TokenType.BLOCK_COMMENT)) {
            current++
        }
    }

    private fun peek(): Token {
        skipComments()
        return if (current < tokens.size) tokens[current] else tokens.last()
    }

    private fun advance(): Token {
        skipComments()
        val token = peek()
        if (!isAtEnd()) current++
        return token
    }

    private fun isAtEnd(): Boolean {
        skipComments()
        return current >= tokens.size || tokens[current].type == TokenType.EOF
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw RuntimeException("$message na linha ${peek().line}, coluna ${peek().column}")
    }

    //
    fun parse(): List<Node> {
        val nodes = mutableListOf<Node>()
        while (!isAtEnd()) {
            nodes.add(parseExpression())
        }
        return nodes
    }

    // Decide qual tipo de expressão/termo está lendo
    private fun parseExpression(): Node {
        val token = peek()
        return when (token.type) {
            TokenType.INT, TokenType.FLOAT -> {
                advance()
                val hasDigits = token.type == TokenType.FLOAT
                Node.Number(token.value.toDouble(), hasDigits = hasDigits, extension = extractExtension())
            }
            TokenType.STRING -> {
                advance()
                Node.Str(token.value, extension = extractExtension())
            }
            TokenType.TRUE -> {
                advance()
                Node.Bool(true, extension = extractExtension())
            }
            TokenType.FALSE -> {
                advance()
                Node.Bool(false, extension = extractExtension())
            }
            TokenType.NULL -> {
                advance()
                Node.Null
            }
            TokenType.LBRACE -> {
                parseHash()
            }
            TokenType.LARR -> {
                parseArray()
            }
            TokenType.AT -> {
                advance() // consume o @
                val name = consume(TokenType.IDENTIFIER, "Esperado o nome da função após '@' na linha ${token.line}")
                val fullName = "@${name.value}"
                if (match(TokenType.LPAREN)) {
                    parseFunction(fullName)
                } else {
                    throw RuntimeException("Esperado parêntese após o identificador '$fullName' na linha ${token.line}")
                }
            }
            TokenType.DOLLAR -> {
                advance() // consume o $
                val segments = mutableListOf<NodePathSegment>()

                // root
                val firstId = consume(TokenType.IDENTIFIER, "Esperado o nome da variável após '$' na linha ${token.line}")
                val rootName = firstId.value

                //
                var extensionName: String? = null
                while (true) {
                    if (match(TokenType.DOT)) {
                        val propToken = consume(TokenType.IDENTIFIER, "Esperado o nome da propriedade após '.'")
                        if (peek().type == TokenType.LPAREN) {
                            extensionName = propToken.value
                            break
                        }
                        segments.add(NodePathSegment.Property(propToken.value))
                    }
                    else if (check(TokenType.LARR)) { // [
                        advance() // consume o [
                        val indexNode = parseExpression() // Permite usar número fixo [0] ou variável [$index]!
                        consume(TokenType.RARR, "Esperado ']' para fechar o índice do array")
                        segments.add(NodePathSegment.Index(indexNode))
                    }
                    else {
                        break
                    }
                }

                Node.Var(rootName, segments, extension = extractExtension(extensionName))
            }
            TokenType.IDENTIFIER -> {
                val name = advance().value
                if (match(TokenType.LPAREN)) {
                    parseFunction(name)
                } else {
                    throw RuntimeException("Esperado parêntese após o identificador '$name'")
                }
            }
            else -> throw RuntimeException("Token inesperado '${token.value}' na linha ${token.line}")
        }
    }

    // Analisa mapas/hashes no formato: { map: "3" }
    private fun parseHash(): Node {
        consume(TokenType.LBRACE, "Esperado '{' para iniciar o Hash")
        val mapData = mutableMapOf<String, Node>()

        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            // Chave do hash pode vir como string ou identificador (ex: map:)
            val keyToken = advance()
            val key = if (keyToken.type == TokenType.STRING || keyToken.type == TokenType.IDENTIFIER) {
                keyToken.value
            } else {
                throw RuntimeException("Chave do hash inválida na linha ${keyToken.line}")
            }

            consume(TokenType.COLON, "Esperado ':' após a chave '$key' no Hash")
            val valueNode = parseExpression()
            mapData[key] = valueNode

            // Se houver vírgula, consome e continua; senão, sai do loop
            if (!match(TokenType.COMMA) || check(TokenType.RARR)) { break }
        }

        consume(TokenType.RBRACE, "Esperado '}' para fechar o Hash")
        return Node.Dict(mapData, extension = extractExtension())
    }

    private fun parseArray(): Node {
        consume(TokenType.LARR, "Esperado '[' para iniciar Array")
        val res = mutableListOf<Node>()
        while (!check(TokenType.RARR) && !isAtEnd()) {
            val value = parseExpression()
            res.add(value)
            if (!match(TokenType.COMMA) || check(TokenType.RARR)) { break }
        }
        consume(TokenType.RARR, "Esperado ']' para fechar Array")

        return Node.Arr(res.toList(), extension = extractExtension())
    }

    // Analisa a estrutura da função: name(params) { children/corpo }
    private fun parseFunction(name: String): Node.Fn {
        val params = parseParams()
        consume(TokenType.RPAREN, "Esperado ')' após os parâmetros da função")

        // Lê o bloco de código da função entre chaves
        val children = mutableListOf<Node>()
        if (peek().type == TokenType.LBRACE) {
            consume(TokenType.LBRACE, "Esperado '{' para iniciar o corpo da função")
            while (!check(TokenType.RBRACE) && !isAtEnd()) {
                children.add(parseExpression())
            }
            consume(TokenType.RBRACE, "Esperado '}' para fechar o corpo da função")
        }

        return Node.Fn(name = name, params = params, children = children, extension = extractExtension())
    }

    private fun parseParams(): List<Node.Param> {
        val res = mutableListOf<Node.Param>()
        while (!check(TokenType.RPAREN) && !isAtEnd()) {
            if (check(TokenType.AT)) {
                val value = parseExpression()
                res.add(Node.Param(null, value))
            }
            else if (check(TokenType.IDENTIFIER)) {
                val maybeKey = advance().value
                if (check(TokenType.LPAREN)) {
                    advance()
                    val value = parseFunction(maybeKey)
                    res.add(Node.Param(null, value))
                } else {
                    consume(TokenType.EQUAL, "Esperado '=' depois de identifier")
                    val value = parseExpression()
                    res.add(Node.Param(maybeKey, value))
                }
            } else {
                val value = parseExpression()
                res.add(Node.Param(null, value))
            }
            //
            if (!match(TokenType.COMMA) || check(TokenType.RARR)) { break }
        }
        return res.toList()
    }

    private fun extractExtension(solvedName: String? = null): Node.Fn? {
        val name = solvedName ?: if (match(TokenType.DOT)) {
            consume(TokenType.IDENTIFIER, "Esperado o nome da extensão após o '.'").value
        } else {
            return null
        }

        if (!match(TokenType.LPAREN)) {
            throw RuntimeException("Esperado parêntese após o identificador da extensão '$name'")
        }

        return parseFunction(name)
    }
}