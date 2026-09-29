package app.adon.suiengine.ast.v2

enum class DslTokenType {
    // Literais & Identificadores
    INT, FLOAT, STRING, TRUE, FALSE, NULL,
    IDENTIFIER,

    // Keywords
    VAR, CONST, FUN,

    // Operadores Aritméticos e Lógicos
    PLUS, MINUS, STAR, SLASH, PERCENT,
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,
    AND_AND, OR_OR,

    // Delimitadores & Símbolos
    LBRACE, RBRACE,
    LPAREN, RPAREN,
    LARR, RARR,
    COLON, COMMA, DOT,
    ARROW,

    IF, ELSE, RETURN,

    // Comentários & EOF
    COMMENT, BLOCK_COMMENT,
    EOF
}

data class Token(
    val type: DslTokenType,
    val value: String,
    val line: Int,
    val column: Int
)

class DslLexer(private val input: String) {

    private var position = 0
    private var line = 1
    private var column = 1
    private val length = input.length

    private fun peek(): Char = if (position < length) input[position] else '\u0000'
    private fun peekNext(): Char = if (position + 1 < length) input[position + 1] else '\u0000'

    private fun advance(): Char {
        val current = peek()
        position++
        if (current == '\n') {
            line++
            column = 1
        } else {
            column++
        }
        return current
    }

    private fun match(expected: Char): Boolean {
        if (peek() == expected) {
            advance()
            return true
        }
        return false
    }

    private fun isAlpha(c: Char) = c in 'a'..'z' || c in 'A'..'Z' || c == '_'
    private fun isDigit(c: Char) = c in '0'..'9'

    fun tokenize(allowComments: Boolean = false): List<Token> {
        val tokens = mutableListOf<Token>()

        while (position < length) {
            val startCol = column
            val c = peek()

            when {
                c.isWhitespace() -> {
                    advance()
                }

                // Identificadores & Keywords
                isAlpha(c) -> {
                    val sb = StringBuilder()
                    while (isAlpha(peek()) || isDigit(peek())) {
                        sb.append(advance())
                    }
                    val text = sb.toString()
                    val type = when (text) {
                        "var" -> DslTokenType.VAR
                        "val" -> DslTokenType.CONST
                        "fun" -> DslTokenType.FUN
                        "true" -> DslTokenType.TRUE
                        "false" -> DslTokenType.FALSE
                        "if" -> DslTokenType.IF
                        "else" -> DslTokenType.ELSE
                        "null" -> DslTokenType.NULL
                        "return" -> DslTokenType.RETURN
                        else -> DslTokenType.IDENTIFIER
                    }
                    tokens.add(Token(type, text, line, startCol))
                }

                // Números Literais
                isDigit(c) -> {
                    val sb = StringBuilder()
                    var isReal = false

                    while (isDigit(peek())) {
                        sb.append(advance())
                    }

                    if (peek() == '.' && isDigit(peekNext())) {
                        isReal = true
                        sb.append(advance()) // consome '.'
                        while (isDigit(peek())) {
                            sb.append(advance())
                        }
                    }

                    val text = sb.toString()
                    val type = if (isReal) DslTokenType.FLOAT else DslTokenType.INT
                    tokens.add(Token(type, text, line, startCol))
                }

                // Strings
                c == '"' -> {
                    advance() // pula aspas de abertura
                    val sb = StringBuilder()

                    while (peek() != '"' && peek() != '\u0000') {
                        if (peek() == '\\') {
                            advance()
                            when (val escaped = advance()) {
                                'n' -> sb.append('\n')
                                't' -> sb.append('\t')
                                'r' -> sb.append('\r')
                                '"' -> sb.append('"')
                                '\\' -> sb.append('\\')
                                else -> sb.append('\\').append(escaped)
                            }
                        } else {
                            sb.append(advance())
                        }
                    }

                    if (!match('"')) {
                        throw RuntimeException("Erro léxico: String não fechada na linha $line, coluna $startCol")
                    }

                    tokens.add(Token(DslTokenType.STRING, sb.toString(), line, startCol))
                }

                // Comentários e Divisão
                c == '/' -> {
                    advance()
                    if (peek() == '/') { // Line comment
                        advance()
                        val sb = StringBuilder()
                        while (peek() != '\n' && peek() != '\u0000') {
                            sb.append(advance())
                        }
                        tokens.add(Token(DslTokenType.COMMENT, sb.toString().trim(), line, startCol))
                    } else if (peek() == '*') { // Block comment
                        advance()
                        val sb = StringBuilder()
                        while (peek() != '\u0000') {
                            if (peek() == '*' && peekNext() == '/') {
                                advance() // consome '*'
                                advance() // consome '/'
                                break
                            }
                            sb.append(advance())
                        }
                        tokens.add(Token(DslTokenType.BLOCK_COMMENT, sb.toString().trim(), line, startCol))
                    } else {
                        tokens.add(Token(DslTokenType.SLASH, "/", line, startCol))
                    }
                }

                // Operadores e Símbolos
                else -> {
                    advance()
                    val tokenType = when (c) {
                        '+' -> DslTokenType.PLUS
                        '-' -> if (match('>')) DslTokenType.ARROW else DslTokenType.MINUS
                        '*' -> DslTokenType.STAR
                        '%' -> DslTokenType.PERCENT

                        '=' -> if (match('=')) DslTokenType.EQUAL_EQUAL else DslTokenType.EQUAL
                        '!' -> if (match('=')) DslTokenType.BANG_EQUAL else DslTokenType.BANG
                        '>' -> if (match('=')) DslTokenType.GREATER_EQUAL else DslTokenType.GREATER
                        '<' -> if (match('=')) DslTokenType.LESS_EQUAL else DslTokenType.LESS

                        '&' -> if (match('&')) DslTokenType.AND_AND else throw RuntimeException("Caractere inesperado '&' na linha $line, coluna $startCol")
                        '|' -> if (match('|')) DslTokenType.OR_OR else throw RuntimeException("Caractere inesperado '|' na linha $line, coluna $startCol")

                        '{' -> DslTokenType.LBRACE
                        '}' -> DslTokenType.RBRACE
                        '(' -> DslTokenType.LPAREN
                        ')' -> DslTokenType.RPAREN
                        '[' -> DslTokenType.LARR
                        ']' -> DslTokenType.RARR
                        ':' -> DslTokenType.COLON
                        ',' -> DslTokenType.COMMA
                        '.' -> DslTokenType.DOT

                        else -> throw RuntimeException("Caractere desconhecido '$c' na linha $line, coluna $startCol")
                    }

                    val tokenText = when (tokenType) {
                        DslTokenType.ARROW -> "->"
                        DslTokenType.EQUAL_EQUAL -> "=="
                        DslTokenType.BANG_EQUAL -> "!="
                        DslTokenType.GREATER_EQUAL -> ">="
                        DslTokenType.LESS_EQUAL -> "<="
                        DslTokenType.AND_AND -> "&&"
                        DslTokenType.OR_OR -> "||"
                        else -> c.toString()
                    }

                    tokens.add(Token(tokenType, tokenText, line, startCol))
                }
            }
        }

        tokens.add(Token(DslTokenType.EOF, "", line, column))

        if (allowComments) return tokens
        return tokens.filter { it.type != DslTokenType.BLOCK_COMMENT && it.type != DslTokenType.COMMENT }
    }
}