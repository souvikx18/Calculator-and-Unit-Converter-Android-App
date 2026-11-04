package com.example

import com.example.domain.calculator.ExpressionEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpressionEvaluatorTest {

    private val evaluator = ExpressionEvaluator()

    @Test
    fun testBasicArithmetic() {
        val res = evaluator.evaluate("12 + 34 × 2 - 10 ÷ 2")
        assertTrue(res is ExpressionEvaluator.EvaluationResult.Success)
        val success = res as ExpressionEvaluator.EvaluationResult.Success
        assertEquals(75.0, success.value, 1e-9)
        assertEquals("75", success.formatted)
    }

    @Test
    fun testParenthesesAndPrecedence() {
        val res = evaluator.evaluate("(2 + 3) × (4 + 5)")
        assertTrue(res is ExpressionEvaluator.EvaluationResult.Success)
        val success = res as ExpressionEvaluator.EvaluationResult.Success
        assertEquals(45.0, success.value, 1e-9)
    }

    @Test
    fun testImplicitMultiplication() {
        val res1 = evaluator.evaluate("2(3 + 4)")
        assertTrue(res1 is ExpressionEvaluator.EvaluationResult.Success)
        assertEquals(14.0, (res1 as ExpressionEvaluator.EvaluationResult.Success).value, 1e-9)

        val res2 = evaluator.evaluate("2π", isDeg = true)
        assertTrue(res2 is ExpressionEvaluator.EvaluationResult.Success)
        assertEquals(2 * Math.PI, (res2 as ExpressionEvaluator.EvaluationResult.Success).value, 1e-9)
    }

    @Test
    fun testTrigonometricDegrees() {
        val sin30 = evaluator.evaluate("sin(30)", isDeg = true)
        assertTrue(sin30 is ExpressionEvaluator.EvaluationResult.Success)
        assertEquals(0.5, (sin30 as ExpressionEvaluator.EvaluationResult.Success).value, 1e-9)

        val cos60 = evaluator.evaluate("cos(60)", isDeg = true)
        assertTrue(cos60 is ExpressionEvaluator.EvaluationResult.Success)
        assertEquals(0.5, (cos60 as ExpressionEvaluator.EvaluationResult.Success).value, 1e-9)

        val tan45 = evaluator.evaluate("tan(45)", isDeg = true)
        assertTrue(tan45 is ExpressionEvaluator.EvaluationResult.Success)
        assertEquals(1.0, (tan45 as ExpressionEvaluator.EvaluationResult.Success).value, 1e-9)
    }

    @Test
    fun testTrigonometricRadians() {
        val sinPiOver2 = evaluator.evaluate("sin(π / 2)", isDeg = false)
        assertTrue(sinPiOver2 is ExpressionEvaluator.EvaluationResult.Success)
        assertEquals(1.0, (sinPiOver2 as ExpressionEvaluator.EvaluationResult.Success).value, 1e-9)
    }

    @Test
    fun testPowerAndRoots() {
        val pow = evaluator.evaluate("2^10")
        assertTrue(pow is ExpressionEvaluator.EvaluationResult.Success)
        assertEquals(1024.0, (pow as ExpressionEvaluator.EvaluationResult.Success).value, 1e-9)

        val sqrt = evaluator.evaluate("√(144)")
        assertTrue(sqrt is ExpressionEvaluator.EvaluationResult.Success)
        assertEquals(12.0, (sqrt as ExpressionEvaluator.EvaluationResult.Success).value, 1e-9)

        val cbrt = evaluator.evaluate("∛(27)")
        assertTrue(cbrt is ExpressionEvaluator.EvaluationResult.Success)
        assertEquals(3.0, (cbrt as ExpressionEvaluator.EvaluationResult.Success).value, 1e-9)
    }

    @Test
    fun testFactorialAndPercentage() {
        val fact = evaluator.evaluate("5!")
        assertTrue(fact is ExpressionEvaluator.EvaluationResult.Success)
        assertEquals(120.0, (fact as ExpressionEvaluator.EvaluationResult.Success).value, 1e-9)

        val pct = evaluator.evaluate("50%")
        assertTrue(pct is ExpressionEvaluator.EvaluationResult.Success)
        assertEquals(0.5, (pct as ExpressionEvaluator.EvaluationResult.Success).value, 1e-9)
    }

    @Test
    fun testDivideByZero() {
        val divZero = evaluator.evaluate("10 ÷ 0")
        assertTrue(divZero is ExpressionEvaluator.EvaluationResult.Error)
        assertEquals("Cannot divide by 0", (divZero as ExpressionEvaluator.EvaluationResult.Error).message)
    }

    @Test
    fun testPreviewEvaluation() {
        val preview = evaluator.evaluatePreview("12 + 34 × 2", isDeg = true)
        assertEquals("80", preview)

        // Preview with trailing unicode minus should trim properly
        val previewMinus = evaluator.evaluatePreview("12 + 34 −", isDeg = true)
        assertEquals("46", previewMinus)
    }

    @Test
    fun testScientificNotationVsEuler() {
        // Scientific notation: 1.5e3 = 1500
        val sci = evaluator.evaluate("1.5e3 + 500")
        assertTrue(sci is ExpressionEvaluator.EvaluationResult.Success)
        assertEquals(2000.0, (sci as ExpressionEvaluator.EvaluationResult.Success).value, 1e-9)

        // Euler's constant: 2e = 2 * E
        val euler = evaluator.evaluate("2e")
        assertTrue(euler is ExpressionEvaluator.EvaluationResult.Success)
        assertEquals(2 * Math.E, (euler as ExpressionEvaluator.EvaluationResult.Success).value, 1e-9)
    }

    @Test
    fun testExactTrigValues() {
        val sin180 = evaluator.evaluate("sin(180)", isDeg = true)
        assertTrue(sin180 is ExpressionEvaluator.EvaluationResult.Success)
        assertEquals(0.0, (sin180 as ExpressionEvaluator.EvaluationResult.Success).value, 1e-12)

        val cos90 = evaluator.evaluate("cos(90)", isDeg = true)
        assertTrue(cos90 is ExpressionEvaluator.EvaluationResult.Success)
        assertEquals(0.0, (cos90 as ExpressionEvaluator.EvaluationResult.Success).value, 1e-12)

        val tan90 = evaluator.evaluate("tan(90)", isDeg = true)
        assertTrue(tan90 is ExpressionEvaluator.EvaluationResult.Error)

        val asin1 = evaluator.evaluate("asin(1)", isDeg = true)
        assertTrue(asin1 is ExpressionEvaluator.EvaluationResult.Success)
        assertEquals(90.0, (asin1 as ExpressionEvaluator.EvaluationResult.Success).value, 1e-9)
    }

    @Test
    fun testUnicodeMinus() {
        val res = evaluator.evaluate("10 − 3")
        assertTrue(res is ExpressionEvaluator.EvaluationResult.Success)
        assertEquals(7.0, (res as ExpressionEvaluator.EvaluationResult.Success).value, 1e-9)
    }
}
