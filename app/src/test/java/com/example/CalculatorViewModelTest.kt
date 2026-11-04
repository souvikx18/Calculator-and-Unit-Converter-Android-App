package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.ui.calculator.CalculatorViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalculatorViewModelTest {

    private lateinit var viewModel: CalculatorViewModel

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = CalculatorViewModel(app)
        viewModel.onClear()
    }

    @Test
    fun testInitialState() {
        assertEquals("", viewModel.expression.value)
        assertNull(viewModel.previewResult.value)
        assertNull(viewModel.errorMessage.value)
        assertNull(viewModel.memoryValue.value)
        assertTrue(viewModel.isDeg.value)
    }

    @Test
    fun testDigitsAndDecimal() {
        viewModel.onDigit("5")
        assertEquals("5", viewModel.expression.value)

        viewModel.onDigit("2")
        assertEquals("52", viewModel.expression.value)

        viewModel.onDigit(".")
        viewModel.onDigit("3")
        assertEquals("52.3", viewModel.expression.value)

        // Multiple decimal points in same number should be blocked
        viewModel.onDigit(".")
        assertEquals("52.3", viewModel.expression.value)
    }

    @Test
    fun testLeadingZeroReplacement() {
        viewModel.onDigit("0")
        assertEquals("0", viewModel.expression.value)

        // Typing another 0 on standalone 0 should not produce "00"
        viewModel.onDigit("0")
        assertEquals("0", viewModel.expression.value)

        // Typing 5 on 0 should replace 0 with 5
        viewModel.onDigit("5")
        assertEquals("5", viewModel.expression.value)

        // Operator + 0 + digit
        viewModel.onOperator("+")
        assertEquals("5+", viewModel.expression.value)
        viewModel.onDigit("0")
        assertEquals("5+0", viewModel.expression.value)
        viewModel.onDigit("7")
        assertEquals("5+7", viewModel.expression.value)
    }

    @Test
    fun testOperatorReplacementWithUnicodeMinus() {
        viewModel.onDigit("9")
        viewModel.onOperator("+")
        assertEquals("9+", viewModel.expression.value)

        // Replace + with − (Unicode minus)
        viewModel.onOperator("−")
        assertEquals("9−", viewModel.expression.value)

        // Replace − with ×
        viewModel.onOperator("×")
        assertEquals("9×", viewModel.expression.value)

        viewModel.onDigit("3")
        assertEquals("9×3", viewModel.expression.value)
    }

    @Test
    fun testEqualsAndPostEqualsChaining() {
        viewModel.onDigit("8")
        viewModel.onOperator("×")
        viewModel.onDigit("5")
        viewModel.onEquals()

        assertEquals("40", viewModel.expression.value)

        // Pressing an operator should chain
        viewModel.onOperator("+")
        assertEquals("40+", viewModel.expression.value)

        viewModel.onDigit("2")
        viewModel.onEquals()
        assertEquals("42", viewModel.expression.value)

        // Pressing a digit after equals should start a fresh calculation
        viewModel.onDigit("7")
        assertEquals("7", viewModel.expression.value)
    }

    @Test
    fun testParenthesesSmartClosing() {
        viewModel.onParenthesis()
        assertEquals("(", viewModel.expression.value)

        viewModel.onDigit("2")
        viewModel.onOperator("+")
        viewModel.onDigit("3")
        assertEquals("(2+3", viewModel.expression.value)

        // Next onParenthesis should close it
        viewModel.onParenthesis()
        assertEquals("(2+3)", viewModel.expression.value)

        // Next onParenthesis should open a new one
        viewModel.onParenthesis()
        assertEquals("(2+3)(", viewModel.expression.value)
    }

    @Test
    fun testPlusMinusNegation() {
        viewModel.onDigit("4")
        viewModel.onDigit("2")
        viewModel.onPlusMinus()
        assertEquals("−42", viewModel.expression.value)

        viewModel.onPlusMinus()
        assertEquals("42", viewModel.expression.value)

        viewModel.onOperator("+")
        viewModel.onDigit("9")
        viewModel.onPlusMinus()
        assertEquals("42+−9", viewModel.expression.value)

        viewModel.onPlusMinus()
        assertEquals("42+9", viewModel.expression.value)
    }

    @Test
    fun testBackspaceFunctions() {
        viewModel.onFunction("sin")
        assertEquals("sin(", viewModel.expression.value)

        viewModel.onBackspace()
        assertEquals("", viewModel.expression.value)

        viewModel.onDigit("1")
        viewModel.onDigit("2")
        viewModel.onBackspace()
        assertEquals("1", viewModel.expression.value)
    }

    @Test
    fun testMemoryOperations() {
        viewModel.onDigit("5")
        viewModel.onDigit("0")
        viewModel.onMemoryStore()
        assertEquals(50.0, viewModel.memoryValue.value)

        viewModel.onClear()
        assertEquals("", viewModel.expression.value)

        viewModel.onMemoryRecall()
        assertEquals("50", viewModel.expression.value)

        viewModel.onClear()
        viewModel.onDigit("2")
        viewModel.onDigit("5")
        viewModel.onMemoryAdd()
        assertEquals(75.0, viewModel.memoryValue.value)

        viewModel.onClear()
        viewModel.onDigit("1")
        viewModel.onDigit("0")
        viewModel.onMemorySubtract()
        assertEquals(65.0, viewModel.memoryValue.value)

        viewModel.onMemoryClear()
        assertNull(viewModel.memoryValue.value)
    }
}
