package app.adon.suiengine.ast.v2

import kotlin.test.Test
import kotlin.test.assertEquals

class DslLexerTest {

    @Test
    fun testTokenizeLiteralsAndOperators() {
        val input = "let price = 42.50"
        val lexer = DslLexer(input)
        val tokens = lexer.tokenize()

        assertEquals(DslTokenType.VAR, tokens[0].type)
        assertEquals(DslTokenType.IDENTIFIER, tokens[1].type)
        assertEquals("price", tokens[1].value)
        assertEquals(DslTokenType.EQUAL, tokens[2].type)
        assertEquals(DslTokenType.FLOAT, tokens[3].type)
        assertEquals("42.50", tokens[3].value)
        assertEquals(DslTokenType.EOF, tokens[4].type)
    }

    @Test
    fun testTokenizeExtensionChaining() {
        val input = "dino(key = 2).curry(2)"
        val lexer = DslLexer(input)
        val tokens = lexer.tokenize()

        assertEquals(DslTokenType.IDENTIFIER, tokens[0].type) // dino
        assertEquals(DslTokenType.LPAREN, tokens[1].type)     // (
        assertEquals(DslTokenType.IDENTIFIER, tokens[2].type) // key
        assertEquals(DslTokenType.EQUAL, tokens[3].type)      // =
        assertEquals(DslTokenType.INT, tokens[4].type)        // 2
        assertEquals(DslTokenType.RPAREN, tokens[5].type)     // )
        assertEquals(DslTokenType.DOT, tokens[6].type)        // .
        assertEquals(DslTokenType.IDENTIFIER, tokens[7].type) // curry
    }

    @Test
    fun testCommentAndStuff() {
        val input = """
            val d = 3
            /*
                some comment
            */
            someFunction().dino {
            }
            if (dino) { cabeludo.call() } else { return 2 }
        """.trimIndent()
        val lexer = DslLexer(input)
        val tokens = lexer.tokenize()

        //
        listOf(
            DslTokenType.IDENTIFIER,
            DslTokenType.IDENTIFIER,
            DslTokenType.EQUAL,
            DslTokenType.INT,

        ).forEachIndexed { index, type ->
            assertEquals(type, tokens[index].type)
        }
    }
}