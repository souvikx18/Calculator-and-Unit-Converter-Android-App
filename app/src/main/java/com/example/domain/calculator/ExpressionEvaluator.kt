package com.example.domain.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

class ExpressionEvaluator {

    sealed class EvaluationResult {
        data class Success(val value: Double, val formatted: String) : EvaluationResult()
        data class Error(val message: String) : EvaluationResult()
    }

    /**
     * Evaluates a mathematical expression string.
     * @param rawExpr e.g. "5 + 3 × sin(30) - 2^3"
     * @param isDeg true if trigonometric angles are in degrees, false for radians
     */
    fun evaluate(rawExpr: String, isDeg: Boolean = true): EvaluationResult {
        if (rawExpr.isBlank()) {
            return EvaluationResult.Success(0.0, "0")
        }

        try {
            val normalized = preprocessExpression(rawExpr)
            val tokens = tokenize(normalized)
            val rpn = shuntingYard(tokens)
            val value = evaluateRPN(rpn, isDeg)

            if (value.isNaN()) {
                return EvaluationResult.Error("Undefined")
            }
            if (value.isInfinite()) {
                return EvaluationResult.Error("Cannot divide by 0")
            }

            val formatted = formatNumber(value)
            return EvaluationResult.Success(value, formatted)
        } catch (e: ArithmeticException) {
            return EvaluationResult.Error(e.message ?: "Math Error")
        } catch (e: IllegalArgumentException) {
            return EvaluationResult.Error(e.message ?: "Invalid Expression")
        } catch (e: Exception) {
            return EvaluationResult.Error("Invalid Expression")
        }
    }

    /**
     * Calculates a preview result for partially typed expressions by attempting
     * to close unclosed parentheses and trimming trailing binary operators.
     */
    fun evaluatePreview(rawExpr: String, isDeg: Boolean = true): String? {
        val trimmed = rawExpr.trim()
        if (trimmed.isEmpty()) return null

        var candidate = trimmed
        // If it ends with an operator like +, -, ×, ÷, ^, −, don't show preview or trim it
        while (candidate.isNotEmpty() && (candidate.last() in "+-×÷/*^,−–")) {
            candidate = candidate.dropLast(1).trim()
        }
        if (candidate.isEmpty()) return null

        // Balance unclosed parentheses
        val openCount = candidate.count { it == '(' }
        val closeCount = candidate.count { it == ')' }
        if (openCount > closeCount) {
            candidate += ")".repeat(openCount - closeCount)
        }

        return when (val res = evaluate(candidate, isDeg)) {
            is EvaluationResult.Success -> {
                // Only show preview if it differs from candidate input itself (unless single number)
                if (res.formatted != candidate) res.formatted else null
            }
            is EvaluationResult.Error -> null
        }
    }

    private fun preprocessExpression(expr: String): String {
        var s = expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("–", "-")
            .replace("π", "PI")
            .replace("√", "sqrt")
            .replace("∛", "cbrt")

        s = REGEX_NUM_PAREN.replace(s, "$1*(")
        s = REGEX_PAREN_PAREN.replace(s, ")*(")
        s = REGEX_PAREN_TOKEN.replace(s, ")*$1")
        s = REGEX_NUM_FUNC.replace(s, "$1*$2")

        return s
    }

    companion object {
        private val REGEX_NUM_PAREN = Regex("(\\d|PI|E|e)\\s*\\(")
        private val REGEX_PAREN_PAREN = Regex("\\)\\s*\\(")
        private val REGEX_PAREN_TOKEN = Regex("\\)\\s*(\\d|PI|E|e|[a-zA-Z])")
        private val REGEX_NUM_FUNC = Regex("(\\d)\\s*(PI|sin|cos|tan|asin|acos|atan|sinh|cosh|tanh|ln|log|sqrt|cbrt|abs|(?i:e)(?![+-]?\\d))")
    }

    private sealed class Token {
        data class Number(val value: Double) : Token()
        data class Operator(val symbol: String, val precedence: Int, val isRightAssociative: Boolean = false) : Token()
        data class Function(val name: String) : Token()
        data class Postfix(val symbol: String) : Token()
        object LeftParen : Token()
        object RightParen : Token()
        object Comma : Token()
    }

    private fun tokenize(expr: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        var expectUnary = true

        while (i < expr.length) {
            val c = expr[i]

            if (c.isWhitespace()) {
                i++
                continue
            }

            if (c.isDigit() || c == '.') {
                val start = i
                var hasDot = (c == '.')
                i++
                while (i < expr.length && (expr[i].isDigit() || (!hasDot && expr[i] == '.'))) {
                    if (expr[i] == '.') hasDot = true
                    i++
                }
                // Check for scientific notation in number: e.g., 1e-4
                if (i < expr.length && (expr[i] == 'e' || expr[i] == 'E') && i + 1 < expr.length) {
                    val nextChar = expr[i + 1]
                    if (nextChar.isDigit() || nextChar == '+' || nextChar == '-') {
                        i++ // consume 'e'
                        if (expr[i] == '+' || expr[i] == '-') i++
                        while (i < expr.length && expr[i].isDigit()) {
                            i++
                        }
                    }
                }
                val numStr = expr.substring(start, i)
                val value = numStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: $numStr")
                tokens.add(Token.Number(value))
                expectUnary = false
                continue
            }

            if (c == '(') {
                tokens.add(Token.LeftParen)
                expectUnary = true
                i++
                continue
            }

            if (c == ')') {
                tokens.add(Token.RightParen)
                expectUnary = false
                i++
                continue
            }

            if (c == ',') {
                tokens.add(Token.Comma)
                expectUnary = true
                i++
                continue
            }

            if (c == '!') {
                tokens.add(Token.Postfix("!"))
                expectUnary = false
                i++
                continue
            }

            if (c == '%') {
                tokens.add(Token.Postfix("%"))
                expectUnary = false
                i++
                continue
            }

            if (c == '+' || c == '-' || c == '*' || c == '/' || c == '^') {
                if (expectUnary) {
                    if (c == '-') {
                        // Unary minus
                        tokens.add(Token.Operator("neg", 4, isRightAssociative = true))
                        i++
                        continue
                    } else if (c == '+') {
                        // Unary plus - skip
                        i++
                        continue
                    }
                }
                val prec = when (c) {
                    '+', '-' -> 1
                    '*', '/' -> 2
                    '^' -> 3
                    else -> 1
                }
                val rightAssoc = (c == '^')
                tokens.add(Token.Operator(c.toString(), prec, rightAssoc))
                expectUnary = true
                i++
                continue
            }

            if (c.isLetter()) {
                val start = i
                while (i < expr.length && expr[i].isLetter()) {
                    i++
                }
                val name = expr.substring(start, i)
                when (name) {
                    "PI" -> {
                        tokens.add(Token.Number(Math.PI))
                        expectUnary = false
                    }
                    "E", "e" -> {
                        tokens.add(Token.Number(Math.E))
                        expectUnary = false
                    }
                    "sin", "cos", "tan", "asin", "acos", "atan",
                    "sinh", "cosh", "tanh", "ln", "log", "sqrt", "cbrt", "abs" -> {
                        tokens.add(Token.Function(name))
                        expectUnary = true
                    }
                    else -> throw IllegalArgumentException("Unknown function: $name")
                }
                continue
            }

            throw IllegalArgumentException("Unexpected character: $c")
        }

        return tokens
    }

    private fun shuntingYard(tokens: List<Token>): List<Token> {
        val output = mutableListOf<Token>()
        val stack = ArrayDeque<Token>()

        for (token in tokens) {
            when (token) {
                is Token.Number -> output.add(token)
                is Token.Postfix -> output.add(token)
                is Token.Function -> stack.addFirst(token)
                is Token.Operator -> {
                    while (stack.isNotEmpty()) {
                        val top = stack.first()
                        if (top is Token.Operator) {
                            if ((!token.isRightAssociative && token.precedence <= top.precedence) ||
                                (token.isRightAssociative && token.precedence < top.precedence)
                            ) {
                                output.add(stack.removeFirst())
                                continue
                            }
                        } else if (top is Token.Function) {
                            output.add(stack.removeFirst())
                            continue
                        }
                        break
                    }
                    stack.addFirst(token)
                }
                is Token.LeftParen -> stack.addFirst(token)
                is Token.RightParen -> {
                    var matchFound = false
                    while (stack.isNotEmpty()) {
                        val top = stack.removeFirst()
                        if (top is Token.LeftParen) {
                            matchFound = true
                            break
                        }
                        output.add(top)
                    }
                    if (!matchFound) throw IllegalArgumentException("Mismatched parentheses")
                    if (stack.isNotEmpty() && stack.first() is Token.Function) {
                        output.add(stack.removeFirst())
                    }
                }
                is Token.Comma -> {
                    while (stack.isNotEmpty() && stack.first() !is Token.LeftParen) {
                        output.add(stack.removeFirst())
                    }
                }
            }
        }

        while (stack.isNotEmpty()) {
            val top = stack.removeFirst()
            if (top is Token.LeftParen || top is Token.RightParen) {
                throw IllegalArgumentException("Mismatched parentheses")
            }
            output.add(top)
        }

        return output
    }

    private fun evaluateRPN(rpn: List<Token>, isDeg: Boolean): Double {
        val stack = ArrayDeque<Double>()

        for (token in rpn) {
            when (token) {
                is Token.Number -> stack.addFirst(token.value)
                is Token.Postfix -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Missing operand")
                    val v = stack.removeFirst()
                    val res = when (token.symbol) {
                        "!" -> factorial(v)
                        "%" -> v / 100.0
                        else -> throw IllegalArgumentException("Unknown postfix: ${token.symbol}")
                    }
                    stack.addFirst(res)
                }
                is Token.Operator -> {
                    if (token.symbol == "neg") {
                        if (stack.isEmpty()) throw IllegalArgumentException("Missing operand for negation")
                        val v = stack.removeFirst()
                        stack.addFirst(-v)
                    } else {
                        if (stack.size < 2) throw IllegalArgumentException("Missing operand for ${token.symbol}")
                        val b = stack.removeFirst()
                        val a = stack.removeFirst()
                        val res = when (token.symbol) {
                            "+" -> a + b
                            "-" -> a - b
                            "*" -> a * b
                            "/" -> {
                                if (b == 0.0) throw ArithmeticException("Cannot divide by 0")
                                a / b
                            }
                            "^" -> a.pow(b)
                            else -> throw IllegalArgumentException("Unknown operator: ${token.symbol}")
                        }
                        stack.addFirst(res)
                    }
                }
                is Token.Function -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Missing argument for ${token.name}")
                    val arg = stack.removeFirst()
                    val res = evalFunction(token.name, arg, isDeg)
                    stack.addFirst(res)
                }
                else -> throw IllegalArgumentException("Unexpected token in evaluation")
            }
        }

        if (stack.size != 1) {
            throw IllegalArgumentException("Invalid expression structure")
        }

        return stack.first()
    }

    private fun evalFunction(name: String, arg: Double, isDeg: Boolean): Double {
        return when (name) {
            "sin" -> {
                if (isDeg) {
                    val norm = ((arg % 360.0) + 360.0) % 360.0
                    when {
                        norm == 0.0 || norm == 180.0 -> 0.0
                        norm == 90.0 -> 1.0
                        norm == 270.0 -> -1.0
                        norm == 30.0 || norm == 150.0 -> 0.5
                        norm == 210.0 || norm == 330.0 -> -0.5
                        else -> {
                            val s = sin(Math.toRadians(arg))
                            if (abs(s) < 1e-15) 0.0 else s
                        }
                    }
                } else {
                    val s = sin(arg)
                    if (abs(s) < 1e-15) 0.0 else s
                }
            }
            "cos" -> {
                if (isDeg) {
                    val norm = ((arg % 360.0) + 360.0) % 360.0
                    when {
                        norm == 90.0 || norm == 270.0 -> 0.0
                        norm == 0.0 -> 1.0
                        norm == 180.0 -> -1.0
                        norm == 60.0 || norm == 300.0 -> 0.5
                        norm == 120.0 || norm == 240.0 -> -0.5
                        else -> {
                            val c = cos(Math.toRadians(arg))
                            if (abs(c) < 1e-15) 0.0 else c
                        }
                    }
                } else {
                    val c = cos(arg)
                    if (abs(c) < 1e-15) 0.0 else c
                }
            }
            "tan" -> {
                if (isDeg) {
                    val norm = ((arg % 180.0) + 180.0) % 180.0
                    if (abs(norm - 90.0) < 1e-9) throw ArithmeticException("Undefined (tan 90°)")
                    if (norm == 0.0) return 0.0
                    if (norm == 45.0) return 1.0
                    if (norm == 135.0) return -1.0
                    val t = tan(Math.toRadians(arg))
                    if (abs(t) < 1e-15) 0.0 else t
                } else {
                    val cosVal = cos(arg)
                    if (abs(cosVal) < 1e-15) throw ArithmeticException("Undefined (tan)")
                    val t = tan(arg)
                    if (abs(t) < 1e-15) 0.0 else t
                }
            }
            "asin" -> {
                if (arg < -1.0000001 || arg > 1.0000001) throw ArithmeticException("Domain error for asin")
                val clamped = arg.coerceIn(-1.0, 1.0)
                val rad = asin(clamped)
                if (isDeg) Math.toDegrees(rad) else rad
            }
            "acos" -> {
                if (arg < -1.0000001 || arg > 1.0000001) throw ArithmeticException("Domain error for acos")
                val clamped = arg.coerceIn(-1.0, 1.0)
                val rad = acos(clamped)
                if (isDeg) Math.toDegrees(rad) else rad
            }
            "atan" -> {
                val rad = atan(arg)
                if (isDeg) Math.toDegrees(rad) else rad
            }
            "sinh" -> sinh(arg)
            "cosh" -> cosh(arg)
            "tanh" -> tanh(arg)
            "ln" -> {
                if (arg <= 0) throw ArithmeticException("Domain error for ln(x) with x <= 0")
                ln(arg)
            }
            "log" -> {
                if (arg <= 0) throw ArithmeticException("Domain error for log(x) with x <= 0")
                log10(arg)
            }
            "sqrt" -> {
                if (arg < 0) throw ArithmeticException("Domain error for sqrt of negative number")
                sqrt(arg)
            }
            "cbrt" -> cbrt(arg)
            "abs" -> abs(arg)
            else -> throw IllegalArgumentException("Unknown function $name")
        }
    }

    private fun factorial(n: Double): Double {
        if (n < 0 || n != floor(n)) {
            throw ArithmeticException("Factorial requires non-negative integer")
        }
        val intN = n.toLong()
        if (intN > 170) {
            throw ArithmeticException("Result overflow for factorial")
        }
        var result = 1.0
        for (i in 2..intN) {
            result *= i.toDouble()
        }
        return result
    }

    fun formatNumber(value: Double): String {
        if (value.isNaN()) return "Undefined"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"
        if (abs(value) == 0.0) return "0"

        val absVal = abs(value)
        val isVeryLarge = absVal >= 1e12
        val isVerySmall = absVal < 1e-6 && absVal > 0.0

        return if (isVeryLarge || isVerySmall) {
            val df = DecimalFormat("0.######E0", DecimalFormatSymbols(Locale.US))
            df.format(value)
        } else {
            // Round to 10 decimal digits to eliminate floating point noise (e.g. 0.1 + 0.2 = 0.3)
            val bd = BigDecimal(value, MathContext(12, RoundingMode.HALF_UP))
                .setScale(10, RoundingMode.HALF_UP)
                .stripTrailingZeros()
            bd.toPlainString()
        }
    }
}
