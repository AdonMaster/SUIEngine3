package app.adon.suiengine.ast.v2

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DslParserTest {

    private fun parse(input: String): List<Dsl.Statement> {
        val lexer = DslLexer(input)
        val tokens = lexer.tokenize()
        val parser = DslParser(tokens)
        return parser.parse()
    }

    // =========================================================================
    // 1. DECLARAÇÕES DE VARIÁVEIS E LITERAIS (Cookbook)
    // =========================================================================

    @Test
    fun testEmptyLambdaAsDefaultInVarAssignment() {
        val ast = parse("val d = {}")

        assertEquals(1, ast.size)
        val varDecl = ast.first() as Dsl.Statement.Var
        assertEquals("d", varDecl.name)
        assertTrue(varDecl.isConstant)

        val lambda = varDecl.initializer as Dsl.Expr.Lambda
        assertTrue(lambda.keys.isEmpty())
        assertTrue(lambda.body.isEmpty())
    }

    @Test
    fun testVarDeclarationsWithLiteralsAndUnary() {
        val code = """
            var identifier = expression
            var array = [1, 2, 3]
            val str = "some"
            val int = -3
            val obj = { some: "thing" }
        """.trimIndent()

        val ast = parse(code)
        assertEquals(5, ast.size)

        // var identifier = expression
        val var1 = ast[0] as Dsl.Statement.Var
        assertEquals("identifier", var1.name)
        assertEquals(false, var1.isConstant)
        assertTrue(var1.initializer is Dsl.Expr.Identifier)

        // var array = [1, 2, 3]
        val var2 = ast[1] as Dsl.Statement.Var
        val arr = (var2.initializer as Dsl.Expr.Literal).value as Dsl.Primitive.Arr
        assertEquals(3, arr.value.size)

        // val str = "some"
        val var3 = ast[2] as Dsl.Statement.Var
        val text = (var3.initializer as Dsl.Expr.Literal).value as Dsl.Primitive.Text
        assertEquals("some", text.value)

        // val int = -3
        val var4 = ast[3] as Dsl.Statement.Var
        val unary = var4.initializer as Dsl.Expr.UnaryOp
        assertEquals("-", unary.operator)
        val num = (unary.right as Dsl.Expr.Literal).value as Dsl.Primitive.Number
        assertEquals(3.0, num.value)

        // val obj = { some: "thing" }
        val var5 = ast[4] as Dsl.Statement.Var
        val dict = (var5.initializer as Dsl.Expr.Literal).value as Dsl.Primitive.Dict
        assertEquals(1, dict.value.size)
        assertTrue(dict.value.containsKey("some"))
    }

    // =========================================================================
    // 2. ESTRUTURAS DE CONTROLE (IF / ELSE / RETURN)
    // =========================================================================

    @Test
    fun testIfElseIfReturnStructure() {
        val code = """
            if (expression) {
                return expression
            } else if (anotherExpression) {
                return
            }
        """.trimIndent()

        val ast = parse(code)
        assertEquals(1, ast.size)

        val ifStmt = ast.first() as Dsl.Statement.If
        assertTrue(ifStmt.condition is Dsl.Expr.Identifier)
        assertEquals(1, ifStmt.thenBlock.size)
        assertTrue(ifStmt.thenBlock.first() is Dsl.Statement.Return)

        // Valida o else if aninhado no elseBlock
        assertEquals(1, ifStmt.elseBlock.size)
        val nestedIf = ifStmt.elseBlock.first() as Dsl.Statement.If
        assertEquals(1, nestedIf.thenBlock.size)

        val returnNull = nestedIf.thenBlock.first() as Dsl.Statement.Return
        val nullLit = (returnNull.value as Dsl.Expr.Literal).value
        assertEquals(Dsl.Primitive.Null, nullLit)
    }

    // =========================================================================
    // 3. LAMBDAS E CHAMADAS DE FUNÇÃO
    // =========================================================================

    @Test
    fun testFunctionCallWithNamedArgumentsAndTrailingExpression() {
        val code = """
            dino(explicitKey = expression, expression)
        """.trimIndent()

        val ast = parse(code)
        assertEquals(1, ast.size)

        val exprStmt = ast.first() as Dsl.Statement.ExpressionStatement
        val call = exprStmt.expr as Dsl.Expr.Call

        assertEquals("dino", call.target)
        assertEquals(2, call.args.size)
        assertEquals("explicitKey", call.args[0].first)
        assertEquals(null, call.args[1].first)
    }

    @Test
    fun testAdvancedLambdaParameters() {
        val code = """
            advanced(lambda = { a ->
                var x = a
            })
            advanced({ a -> })
            advanced({})
        """.trimIndent()

        val ast = parse(code)
        assertEquals(3, ast.size)

        // 1. Lambda nomeada com parâmetro e statement no corpo
        val call1 = (ast[0] as Dsl.Statement.ExpressionStatement).expr as Dsl.Expr.Call
        val lambda1 = call1.args.first().second as Dsl.Expr.Lambda
        assertEquals(listOf("a"), lambda1.keys)
        assertEquals(1, lambda1.body.size)

        // 2. Lambda posicional com parâmetro e corpo vazio
        val call2 = (ast[1] as Dsl.Statement.ExpressionStatement).expr as Dsl.Expr.Call
        val lambda2 = call2.args.first().second as Dsl.Expr.Lambda
        assertEquals(listOf("a"), lambda2.keys)
        assertTrue(lambda2.body.isEmpty())

        // 3. Lambda posicional vazia
        val call3 = (ast[2] as Dsl.Statement.ExpressionStatement).expr as Dsl.Expr.Call
        val lambda3 = call3.args.first().second as Dsl.Expr.Lambda
        assertTrue(lambda3.keys.isEmpty())
        assertTrue(lambda3.body.isEmpty())
    }

    // =========================================================================
    // 4. ACCESSORS, CURRYING E EXTENSIONS
    // =========================================================================

    @Test
    fun testExtensionsAndAccessors() {
        val code = """
            a.b().c().d()
            a.b().dino
            a.b()[3]
        """.trimIndent()

        val ast = parse(code)
        assertEquals(3, ast.size)

        // 1. a.b().c().d() -> Currying / Chamadas encadeadas
        val expr1 = (ast[0] as Dsl.Statement.ExpressionStatement).expr as Dsl.Expr.Identifier
        assertEquals("a", expr1.name)
        assertEquals(3, expr1.extension.size)
        assertTrue(expr1.extension[0] is Dsl.Extension.Fn)
        assertTrue(expr1.extension[1] is Dsl.Extension.Fn)
        assertTrue(expr1.extension[2] is Dsl.Extension.Fn)

        // 2. a.b().dino -> Accessor de propriedade
        val expr2 = (ast[1] as Dsl.Statement.ExpressionStatement).expr as Dsl.Expr.Identifier
        assertEquals(2, expr2.extension.size)
        assertTrue(expr2.extension[0] is Dsl.Extension.Fn)
        assertTrue(expr2.extension[1] is Dsl.Extension.Property)

        // 3. a.b()[3] -> Accessor de índice/array
        val expr3 = (ast[2] as Dsl.Statement.ExpressionStatement).expr as Dsl.Expr.Identifier
        assertEquals(2, expr3.extension.size)
        assertTrue(expr3.extension[0] is Dsl.Extension.Fn)
        val access = expr3.extension[1] as Dsl.Extension.Index
        val indexVal = ((access.key as Dsl.Expr.Literal).value as Dsl.Primitive.Number).value
        assertEquals(3.0, indexVal)
    }

    // =========================================================================
    // 5. REGRA DE VALIDAÇÃO DE EXPRESSÕES SOLTAS (Middle of Block)
    // =========================================================================

    @Test
    fun testValidExpressionAtTheEndOfBlock() {
        val code = """
            fun dino() {
                var d = 55
                d + 55
            }
        """.trimIndent()

        val ast = parse(code)
        val fn = ast.first() as Dsl.Statement.Fn
        assertEquals(2, fn.body.size)
        assertTrue(fn.body[0] is Dsl.Statement.Var)
        assertTrue(fn.body[1] is Dsl.Statement.ExpressionStatement)
    }

    @Test
    fun testInvalidLooseExpressionsInMiddleOfBlockFails() {
        val code = """
            fun dino() {
                "this time i know it's for real"
                var x = 10
            }
        """.trimIndent()

        // Garante que expressões numéricas soltas no meio do bloco lançam erro sintático
        assertFailsWith<RuntimeException> {
            parse(code)
        }
    }
}