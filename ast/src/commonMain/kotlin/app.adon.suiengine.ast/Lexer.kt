package app.adon.suiengine.ast

import com.benasher44.uuid.uuid4

enum class TokenType {
    INT, REAL, STRING, TRUE, FALSE, NULL,

    IDENTIFIER,

    AT, EQUAL, DOLLAR,

    LBRACE, RBRACE,
    LPAREN, RPAREN,
    LARR, RARR,

    COLON, COMMA,

    COMMENT, BLOCK_COMMENT,

    EOF;

}

data class Token(val type: TokenType, val value: String, val line: Int, val column: Int)

class Lexer(private val input: String) {

    private var position = 0
    private var line = 1
    private var column = 1
    private val length = input.length

    private fun peek(): Char = if (position < length) input[position] else '\u0000'
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

    private fun isAlpha(c: Char) = c in 'a'..'z' || c in 'A'..'Z' || c == '_'
    private fun isDigit(c: Char) = c in '0'..'9'

    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()
        while (position < length) {
            val c = peek()
            when {
                // Ignorar espaços em branco, quebras de linha e tabulações
                c.isWhitespace() -> {
                    advance()
                }
                // Identificadores ou Palavras-chave (ex: function_name, true, false, null)
                isAlpha(c) -> {
                    val startCol = column
                    val sb = StringBuilder()
                    while (isAlpha(peek()) || isDigit(peek())) {
                        sb.append(advance())
                    }
                    val text = sb.toString()
                    val type = when (text) {
                        "true" -> TokenType.TRUE
                        "false" -> TokenType.FALSE
                        "null" -> TokenType.NULL
                        else -> TokenType.IDENTIFIER
                    }
                    tokens.add(Token(type, text, line, startCol))
                }
                // Números (Inteiros ou Reais)
                isDigit(c) || c == '-' -> {
                    val startCol = column
                    val sb = StringBuilder()

                    // negatives
                    if (c == '-') {
                        sb.append(advance())
                    }
                    if (isDigit(peek())) {
                        var isReal = false
                        while (isDigit(peek()) || peek() == '.') {
                            if (peek() == '.') {
                                if (isReal) break // segundo ponto inválido no número
                                isReal = true
                            }
                            sb.append(advance())
                        }
                        val text = sb.toString()
                        val type = if (isReal) TokenType.REAL else TokenType.INT
                        tokens.add(Token(type, text, line, startCol))
                    } else {
                        throw RuntimeException("Operador ou caractere inesperado na linha $line, coluna $startCol")
                    }
                }
                // comment sesion
                c == '/' -> {
                    val startCol = column
                    advance()
                    val sb = StringBuilder()
                    // line comment
                    if (peek() == '/') {
                        advance()
                        while (peek() != '\n' && peek() != '\u0000') {
                            sb.append(advance())
                        }
                        tokens.add(Token(TokenType.COMMENT, sb.toString().trim(), line, startCol))
                    }
                    // block comment
                    else if (peek() == '*') {
                        advance()
                        while (peek() != '\u0000') {
                            if (peek() == '*') {
                                advance()
                                if (peek() == '/') {
                                    advance()
                                    break
                                };
                                sb.append('*')
                            }
                            sb.append(advance())
                        }
                        tokens.add(Token(TokenType.BLOCK_COMMENT, sb.toString().trim(), line, startCol))
                    } else {
                        throw RuntimeException("Erro léxico: Comentário não iniciado corretamente $line, coluna $startCol")
                    }
                }
                // Strings entre aspas duplas
                c == '"' -> {
                    val startCol = column
                    advance() // pula aspas de abertura
                    val sb = StringBuilder()

                    while (peek() != '"' && peek() != '\u0000') {
                        val current = peek()
                        if (current == '\\') {
                            advance() // consome a barra invertida
                            when (val escapedChar = advance()) { // pega o próximo caractere
                                'n' -> sb.append('\n')
                                't' -> sb.append('\t')
                                'r' -> sb.append('\r')
                                '"' -> sb.append('"')
                                '\\' -> sb.append('\\')
                                else -> {
                                    // Se for um escape desconhecido, você pode ignorar a barra ou lançar erro
                                    sb.append('\\').append(escapedChar)
                                }
                            }
                        } else {
                            sb.append(advance())
                        }
                    }
                    if (peek() == '"') {
                        advance() // pula a aspas de fechamento
                    } else {
                        throw RuntimeException("Erro léxico: String não fechada na linha $line, coluna $startCol")
                    }
                    tokens.add(Token(TokenType.STRING, sb.toString(), line, startCol))
                }
                // Símbolos individuais
                else -> {
                    val startCol = column
                    val tokenType = when (c) {
                        '{' -> TokenType.LBRACE
                        '}' -> TokenType.RBRACE
                        '(' -> TokenType.LPAREN
                        ')' -> TokenType.RPAREN
                        '[' -> TokenType.LARR
                        ']' -> TokenType.RARR
                        ':' -> TokenType.COLON
                        ',' -> TokenType.COMMA
                        '@' -> TokenType.AT
                        '=' -> TokenType.EQUAL
                        '$' -> TokenType.DOLLAR
                        else -> throw RuntimeException("Caractere desconhecido '$c' na linha $line, coluna $startCol")
                    }
                    advance()
                    tokens.add(Token(tokenType, c.toString(), line, startCol))
                }
            }
        }
        tokens.add(Token(TokenType.EOF, "", line, column))
        return tokens
    }

}