package app.adon.suiengine.ast.v2

private enum class Precedence {
    NONE,
    ASSIGNMENT, // =
    OR,         // ||
    AND,        // &&
    EQUALITY,   // == !=
    COMPARISON, // < > <= >=
    TERM,       // + -
    FACTOR,     // * / %
    UNARY,      // ! -
    CALL        // . () []
}

class DslParser(private val tokens: List<Token>) {

    private var current = 0

    private fun peek(): Token = tokens[current]
    private fun previous(): Token = tokens[if (current > 0) current - 1 else 0]
    private fun isAtEnd(): Boolean = peek().type == DslTokenType.EOF

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun check(type: DslTokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun checkNext(type: DslTokenType): Boolean = checkOffset(1, type)

    private fun checkOffset(offset: Int, type: DslTokenType): Boolean {
        val targetIndex = current + offset
        if (targetIndex >= tokens.size) return false
        return tokens[targetIndex].type == type
    }

    private fun match(vararg types: DslTokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun consume(type: DslTokenType, message: String): Token {
        if (check(type)) return advance()
        val t = peek()
        throw RuntimeException("Erro Sintático [$message] na linha${t.line}, coluna ${t.column}. Encontrado: '${t.value}'")
    }

    fun parse(): List<Dsl.Statement> {
        val statements = mutableListOf<Dsl.Statement>()
        while (!isAtEnd()) {
            statements.add(parseStatement())
        }
        return statements
    }

    // =========================================================================
    // STATEMENTS
    // =========================================================================

    private fun parseStatement(): Dsl.Statement {
        return when {
            match(DslTokenType.VAR) -> parseVarDeclaration(isConstant = false)
            match(DslTokenType.CONST) -> parseVarDeclaration(isConstant = true)
            match(DslTokenType.FUN) -> parseFunctionDeclaration()
            match(DslTokenType.IF) -> parseIfStatement()
            match(DslTokenType.RETURN) -> parseReturnStatement()
            check(DslTokenType.IDENTIFIER) && checkNext(DslTokenType.EQUAL) -> parseAssignmentStatement()
            else -> parseExpressionStatement()
        }
    }

    private fun parseVarDeclaration(isConstant: Boolean): Dsl.Statement.Var {
        val nameToken = consume(DslTokenType.IDENTIFIER, "Esperado nome do identificador na declaração de variável")
        val initializer = if (match(DslTokenType.EQUAL)) {
            parseExpression()
        } else {
            Dsl.Expr.Literal(Dsl.Primitive.Null)
        }
        return Dsl.Statement.Var(nameToken.value, isConstant, initializer)
    }

    private fun parseAssignmentStatement(): Dsl.Statement.Assignment {
        val nameToken = consume(DslTokenType.IDENTIFIER, "Esperado nome da variável para atribuição")
        consume(DslTokenType.EQUAL, "Esperado '=' após identificador")
        val value = parseExpression()
        return Dsl.Statement.Assignment(nameToken.value, value)
    }

    private fun parseReturnStatement(): Dsl.Statement.Return {
        val value = if (check(DslTokenType.RBRACE) || check(DslTokenType.EOF)) {
            Dsl.Expr.Literal(Dsl.Primitive.Null)
        } else {
            parseExpression()
        }
        return Dsl.Statement.Return(value)
    }

    private fun parseIfStatement(): Dsl.Statement.If {
        consume(DslTokenType.LPAREN, "Esperado '(' após 'if'")
        val condition = parseExpression()
        consume(DslTokenType.RPAREN, "Esperado ')' após condição do 'if'")

        consume(DslTokenType.LBRACE, "Esperado '{' para iniciar bloco do 'if'")
        val thenBlock = parseBlock()

        val elseBlock = if (match(DslTokenType.ELSE)) {
            if (check(DslTokenType.IF)) {
                // else if -> encapsulado como um statement dentro do bloco else
                advance()
                listOf(parseIfStatement())
            } else {
                consume(DslTokenType.LBRACE, "Esperado '{' para iniciar bloco do 'else'")
                parseBlock()
            }
        } else {
            emptyList()
        }

        return Dsl.Statement.If(condition, thenBlock, elseBlock)
    }

    private fun parseFunctionDeclaration(): Dsl.Statement.Fn {
        val nameToken = consume(DslTokenType.IDENTIFIER, "Esperado nome da função")
        consume(DslTokenType.LPAREN, "Esperado '(' após nome da função")

        val params = mutableListOf<Pair<String, Dsl.Expr>>()
        if (!check(DslTokenType.RPAREN)) {
            do {
                val paramName = consume(DslTokenType.IDENTIFIER, "Esperado nome do parâmetro").value
                val defaultValue = if (match(DslTokenType.EQUAL)) {
                    parseExpression()
                } else {
                    Dsl.Expr.Literal(Dsl.Primitive.Null)
                }
                params.add(paramName to defaultValue)
            } while (match(DslTokenType.COMMA))
        }
        consume(DslTokenType.RPAREN, "Esperado ')' após parâmetros da função")

        consume(DslTokenType.LBRACE, "Esperado '{' no corpo da função")
        val body = parseBlock()

        return Dsl.Statement.Fn(nameToken.value, params, body)
    }

    private fun parseExpressionStatement(): Dsl.Statement.ExpressionStatement {
        val expr = parseExpression()
        return Dsl.Statement.ExpressionStatement(expr)
    }

    private fun parseBlock(): List<Dsl.Statement> {
        val statements = mutableListOf<Dsl.Statement>()

        while (!check(DslTokenType.RBRACE) && !isAtEnd()) {
            val stmt = parseStatement()
            val isLast = check(DslTokenType.RBRACE)

            // Se for um ExpressionStatement, valida se está na posição permitida (último item do bloco)
            if (stmt is Dsl.Statement.ExpressionStatement) {
                // Se você quiser permitir chamadas de função soltas (ex: print("hello")), pode verificar aqui.
                // Se for apenas um literal ou expressão sem efeito de lado e NÃO for o último item:
                if (!isLast) {
                    val t = previous()
                    throw RuntimeException(
                        "Erro Sintático: Expressões soltas sem efeito de lado não são permitidas no meio do bloco. " +
                                "Apenas a última expressão de um bloco é permitida (linha ${t.line}, coluna ${t.column})."
                    )
                }
            }

            statements.add(stmt)
        }

        consume(DslTokenType.RBRACE, "Esperado '}' após fechar o bloco")
        return statements
    }

    // =========================================================================
    // EXPRESSIONS & PRECEDENCE (PRATT PARSER)
    // =========================================================================

    private fun parseExpression(precedence: Precedence = Precedence.NONE): Dsl.Expr {
        var expr = parsePrefix()

        while (!isAtEnd() && precedence.ordinal < getPrecedence(peek().type).ordinal) {
            expr = parseInfix(expr)
        }

        return expr
    }

    private fun parsePrefix(): Dsl.Expr {
        val token = advance()
        val expr = when (token.type) {
            DslTokenType.INT -> {
                val longValue = token.value.toLongOrNull()
                    ?: throw RuntimeException("Erro léxico/sintático: O número inteiro '${token.value}' excede o limite suportado (Long) na linha ${token.line}, coluna ${token.column}")
                Dsl.Expr.Literal(Dsl.Primitive.Number.Integer(longValue))
            }
            DslTokenType.FLOAT -> {
                val doubleValue = token.value.toDoubleOrNull()
                    ?: throw RuntimeException("Erro léxico/sintático: O número ponto flutuante '${token.value}' excede o limite suportado (Double) na linha ${token.line}, coluna ${token.column}")
                Dsl.Expr.Literal(Dsl.Primitive.Number.Real(doubleValue))
            }
            DslTokenType.STRING -> Dsl.Expr.Literal(Dsl.Primitive.Text(token.value))
            DslTokenType.TRUE -> Dsl.Expr.Literal(Dsl.Primitive.Bool(true))
            DslTokenType.FALSE -> Dsl.Expr.Literal(Dsl.Primitive.Bool(false))
            DslTokenType.NULL -> Dsl.Expr.Literal(Dsl.Primitive.Null)

            DslTokenType.MINUS, DslTokenType.BANG -> {
                val operator = token.value
                val right = parseExpression(Precedence.UNARY)
                Dsl.Expr.UnaryOp(operator, right)
            }

            DslTokenType.IDENTIFIER -> {
                if (check(DslTokenType.LPAREN)) {
                    parseCallExpression(token.value)
                } else {
                    Dsl.Expr.Identifier(token.value)
                }
            }

            DslTokenType.LARR -> parseArrayLiteral()
            DslTokenType.LBRACE -> parseObjectOrLambda()

            DslTokenType.LPAREN -> {
                val innerExpr = parseExpression()
                consume(DslTokenType.RPAREN, "Esperado ')' após expressão entre parênteses")
                innerExpr
            }

            else -> throw RuntimeException("Expressão inválida iniciada por '${token.value}' na linha ${token.line}, coluna${token.column}")
        }

        return parseExtensions(expr)
    }

    private fun parseInfix(left: Dsl.Expr): Dsl.Expr {
        val token = advance()
        val precedence = getPrecedence(token.type)
        val right = parseExpression(precedence)

        val operator = when (token.type) {
            DslTokenType.PLUS -> Operator.SUM
            DslTokenType.MINUS -> Operator.SUB
            DslTokenType.STAR -> Operator.MUL
            DslTokenType.SLASH -> Operator.DIV
            DslTokenType.PERCENT -> Operator.MOD
            DslTokenType.AND_AND -> Operator.AND
            DslTokenType.OR_OR -> Operator.OR
            DslTokenType.EQUAL_EQUAL -> Operator.EQ
            DslTokenType.BANG_EQUAL -> Operator.NEQ
            DslTokenType.GREATER -> Operator.GT
            DslTokenType.LESS -> Operator.LT
            DslTokenType.GREATER_EQUAL -> Operator.GTE
            DslTokenType.LESS_EQUAL -> Operator.LTE
            else -> throw RuntimeException("Operador infixo não suportado: ${token.value}")
        }

        val binaryExpr = Dsl.Expr.BinaryOp(left, operator, right)
        return parseExtensions(binaryExpr)
    }

    private fun getPrecedence(type: DslTokenType): Precedence = when (type) {
        DslTokenType.OR_OR -> Precedence.OR
        DslTokenType.AND_AND -> Precedence.AND
        DslTokenType.EQUAL_EQUAL, DslTokenType.BANG_EQUAL -> Precedence.EQUALITY
        DslTokenType.LESS, DslTokenType.LESS_EQUAL, DslTokenType.GREATER, DslTokenType.GREATER_EQUAL -> Precedence.COMPARISON
        DslTokenType.PLUS, DslTokenType.MINUS -> Precedence.TERM
        DslTokenType.STAR, DslTokenType.SLASH, DslTokenType.PERCENT -> Precedence.FACTOR
        else -> Precedence.NONE
    }

    // =========================================================================
    // AUXILIARY LITERALS & EXTENSIONS
    // =========================================================================

    private fun parseCallExpression(targetName: String): Dsl.Expr.Call {
        consume(DslTokenType.LPAREN, "Esperado '(' na chamada de função")
        val args = mutableListOf<Pair<String?, Dsl.Expr>>()

        if (!check(DslTokenType.RPAREN)) {
            do {
                var key: String? = null
                if (check(DslTokenType.IDENTIFIER) && checkNext(DslTokenType.EQUAL)) {
                    key = advance().value
                    advance() // consome '='
                }
                val expr = parseExpression()
                args.add(key to expr)
            } while (match(DslTokenType.COMMA))
        }

        consume(DslTokenType.RPAREN, "Esperado ')' após argumentos")
        return Dsl.Expr.Call(targetName, args)
    }

    private fun parseArrayLiteral(): Dsl.Expr.Literal {
        val elements = mutableListOf<Dsl.Expr>()
        if (!check(DslTokenType.RARR)) {
            do {
                elements.add(parseExpression())
            } while (match(DslTokenType.COMMA))
        }
        consume(DslTokenType.RARR, "Esperado ']' para fechar o array")
        return Dsl.Expr.Literal(Dsl.Primitive.Arr(elements))
    }

    /**
     * Resolve a ambiguidade de `{}`:
     * Pode ser um Dictionary `{ key: "value" }` ou uma Lambda `{ a -> ... }` ou `{ statement }`.
     * Por padrão (como no cookbook `{}`), resolve para Lambda sem parâmetros.
     */
    private fun parseObjectOrLambda(): Dsl.Expr {
        // Se for um bloco vazio ({}), prioriza ser Lambda sem parâmetros conforme especificado
        if (check(DslTokenType.RBRACE)) {
            advance() // consome '}'
            return Dsl.Expr.Lambda(emptyList(), emptyList())
        }

        // Verifica se é um dicionário baseado no padrão `{ IDENTIFIER : ... }` ou `{ STRING : ... }`
        val isDict = (check(DslTokenType.IDENTIFIER) || check(DslTokenType.STRING)) && checkNext(DslTokenType.COLON)

        if (isDict) {
            val map = mutableMapOf<String, Dsl.Expr>()
            do {
                val keyToken = advance()
                consume(DslTokenType.COLON, "Esperado ':' após a chave do dicionário")
                val valueExpr = parseExpression()
                map[keyToken.value] = valueExpr
            } while (match(DslTokenType.COMMA))

            consume(DslTokenType.RBRACE, "Esperado '}' após fechar dicionário")
            return Dsl.Expr.Literal(Dsl.Primitive.Dict(map))
        }

        // Caso contrário, trata como Lambda
        val keys = mutableListOf<String>()

        // Checa assinatura com parâmetro -> `a ->` ou `a, b ->`
        if (check(DslTokenType.IDENTIFIER) && (checkNext(DslTokenType.ARROW) || checkNext(DslTokenType.COMMA))) {
            do {
                keys.add(advance().value)
            } while (match(DslTokenType.COMMA))
            consume(DslTokenType.ARROW, "Esperado '->' após parâmetros da lambda")
        }

        val body = mutableListOf<Dsl.Statement>()
        while (!check(DslTokenType.RBRACE) && !isAtEnd()) {
            body.add(parseStatement())
        }

        consume(DslTokenType.RBRACE, "Esperado '}' para fechar a lambda")
        return Dsl.Expr.Lambda(keys, body)
    }

    /**
     * Adiciona suporte a extensões como `.key`, `[index]` ou chamadas seguidas `.fn()`
     */
    private fun parseExtensions(baseExpr: Dsl.Expr): Dsl.Expr {
        var expr = baseExpr
        val extensions = mutableListOf<Dsl.Extension>()

        while (true) {
            when {
                // Acesso via Ponto: .member ou .member(...)
                match(DslTokenType.DOT) -> {
                    val memberToken = consume(
                        DslTokenType.IDENTIFIER,
                        "Esperado identificador/nome de propriedade após '.'"
                    )

                    if (check(DslTokenType.LPAREN)) {
                        // Chamada de método: .fn(args)
                        val call = parseCallExpression(memberToken.value)
                        extensions.add(Dsl.Extension.Fn(call))
                    } else {
                        // Acesso a propriedade direta: .someProperty
                        extensions.add(Dsl.Extension.Property(memberToken.value))
                    }
                }

                // Acesso Indexado Dinâmico: [expression]
                match(DslTokenType.LARR) -> {
                    val accessExpr = parseExpression()
                    consume(DslTokenType.RARR, "Esperado ']' após expressão de índice")
                    extensions.add(Dsl.Extension.Index(accessExpr))
                }

                // Currying / Invocação Direta sem ponto: fn()(args)
                check(DslTokenType.LPAREN) -> {
                    val call = parseCallExpression(targetName = "")
                    extensions.add(Dsl.Extension.Fn(call))
                }

                else -> break
            }
        }

        if (extensions.isEmpty()) return expr

        // Atribui as extensões preservando o nó base
        return when (expr) {
            is Dsl.Expr.Literal -> expr.copy(extension = expr.extension + extensions)
            is Dsl.Expr.Identifier -> expr.copy(extension = expr.extension + extensions)
            is Dsl.Expr.Call -> expr.copy(extension = expr.extension + extensions)
            is Dsl.Expr.Lambda -> expr.copy(extension = expr.extension + extensions)
            is Dsl.Expr.BinaryOp -> expr.copy(extension = expr.extension + extensions)
            is Dsl.Expr.UnaryOp -> expr.copy(extension = expr.extension + extensions)
        }
    }
}