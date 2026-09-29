package app.adon.suiengine.ast.v2

enum class Operator {
    SUM, SUB, MUL, DIV, MOD,
    AND, OR,
    EQ, NEQ, GT, LT, GTE, LTE
}

sealed interface Dsl {

    sealed interface Statement : Dsl {
        data class Var(
            val name: String,
            val isConstant: Boolean,
            val initializer: Expr = Expr.Literal(Primitive.Null)
        ) : Statement

        data class Assignment(
            val target: String,
            val value: Expr
        ) : Statement

        data class Return(
            val value: Expr = Expr.Literal(Primitive.Null)
        ): Statement

        data class ExpressionStatement(
            val expr: Expr
        ) : Statement

        data class Fn(
            val name: String,
            val params: List<Pair<String, Expr>>,
            val body: List<Statement>,
        ) : Statement

        data class If(
            val condition: Expr,
            val thenBlock: List<Dsl>,
            val elseBlock: List<Dsl> = emptyList() // Pode conter outros Statements, inclusive outro If!
        ) : Statement
    }

    sealed interface Primitive {
        sealed interface Number : Primitive {
            val doubleValue: Double
            operator fun plus(other: Number): Number
            operator fun minus(other: Number): Number
            operator fun times(other: Number): Number
            operator fun div(other: Number): Number
            operator fun rem(other: Number): Number
            operator fun unaryMinus(): Number
            data class Integer(val value: Long) : Number {
                override val doubleValue: Double get() = value.toDouble()
                override fun plus(other: Number): Number = when (other) {
                    is Integer -> Integer(this.value + other.value)
                    is Real -> Real(this.value + other.value)
                }
                override fun minus(other: Number): Number = when (other) {
                    is Integer -> Integer(this.value - other.value)
                    is Real -> Real(this.value - other.value)
                }
                override fun times(other: Number): Number = when (other) {
                    is Integer -> Integer(this.value * other.value)
                    is Real -> Real(this.value * other.value)
                }
                override fun div(other: Number): Number = when (other) {
                    // Divisão entre dois inteiros: se for exata mantém Integer, senão promove a Real
                    is Integer -> {
                        if (other.value == 0L) throw ArithmeticException("Divisão por zero")
                        if (this.value % other.value == 0L) Integer(this.value / other.value)
                        else Real(this.value.toDouble() / other.value.toDouble())
                    }
                    is Real -> Real(this.value / other.value)
                }
                override fun rem(other: Number): Number = when (other) {
                    is Integer -> Integer(this.value % other.value)
                    is Real -> Real(this.value % other.value)
                }
                override fun unaryMinus(): Number = Integer(-value)
                override fun toString(): String = value.toString()
            }

            data class Real(val value: Double) : Number {
                override val doubleValue: Double get() = value
                override fun plus(other: Number): Number = Real(this.value + other.doubleValue)
                override fun minus(other: Number): Number = Real(this.value - other.doubleValue)
                override fun times(other: Number): Number = Real(this.value * other.doubleValue)
                override fun div(other: Number): Number = Real(this.value / other.doubleValue)
                override fun rem(other: Number): Number = Real(this.value % other.doubleValue)
                override fun unaryMinus(): Number = Real(-value)
                override fun toString(): String = value.toString()
            }
        }
        data class Text(val value: String) : Primitive
        data class Bool(val value: Boolean) : Primitive
        data class Arr(val value: List<Expr>) : Primitive
        data class Dict(val value: Map<String, Expr>) : Primitive
        data object Null : Primitive
    }

    sealed interface Extension {
        data class Property(val key: String) : Extension
        data class Index(val key: Expr) : Extension
        data class Fn(val caller: Expr.Call): Extension
    }

    sealed interface Expr : Dsl {

        val extension: List<Extension>

        data class Literal(
            val value: Primitive,
            override val extension: List<Extension> = emptyList()
        ) : Expr

        data class Identifier(
            val name: String,
            override val extension: List<Extension> = emptyList()
        ) : Expr

        data class Call(
            val target: String,
            val args: List<Pair<String?, Expr>> = emptyList(),
            override val extension: List<Extension> = emptyList()
        ) : Expr

        data class Lambda(
            val keys: List<String>,
            val body: List<Statement>,
            override val extension: List<Extension> = emptyList()
        ) : Expr

        data class BinaryOp(
            val left: Expr,
            val operator: Operator,
            val right: Expr,
            override val extension: List<Extension> = emptyList()
        ) : Expr

        data class UnaryOp(
            val operator: String,
            val right: Expr,
            override val extension: List<Extension> = emptyList()
        ) : Expr

    }
}