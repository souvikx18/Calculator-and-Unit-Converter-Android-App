package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.domain.converter.UnitCategory
import com.example.domain.converter.UnitConverterEngine
import com.example.ui.converter.ConverterViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ConverterViewModelTest {

    private lateinit var viewModel: ConverterViewModel

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = ConverterViewModel(app)
    }

    @Test
    fun testInitialState() {
        assertEquals(UnitCategory.LENGTH, viewModel.selectedCategory.value)
        assertEquals("1", viewModel.inputValue.value)
        assertTrue(viewModel.outputValue.value.isNotEmpty())
        assertTrue(viewModel.availableUnits.value.size >= 2)
    }

    @Test
    fun testKeyInputDigits() {
        viewModel.onKeyInput("C")
        assertEquals("0", viewModel.inputValue.value)

        viewModel.onKeyInput("5")
        assertEquals("5", viewModel.inputValue.value)

        viewModel.onKeyInput("2")
        assertEquals("52", viewModel.inputValue.value)

        viewModel.onKeyInput(".")
        viewModel.onKeyInput("8")
        assertEquals("52.8", viewModel.inputValue.value)

        // Multiple decimal points prevented
        viewModel.onKeyInput(".")
        assertEquals("52.8", viewModel.inputValue.value)

        // Test 00 input
        viewModel.onKeyInput("C")
        assertEquals("0", viewModel.inputValue.value)
        viewModel.onKeyInput("00")
        assertEquals("0", viewModel.inputValue.value) // Should not become 00
        viewModel.onKeyInput("5")
        viewModel.onKeyInput("00")
        assertEquals("500", viewModel.inputValue.value)
    }

    @Test
    fun testKeyInputDeleteAndClear() {
        viewModel.onKeyInput("C")
        viewModel.onKeyInput("1")
        viewModel.onKeyInput("2")
        viewModel.onKeyInput("3")
        assertEquals("123", viewModel.inputValue.value)

        viewModel.onKeyInput("DEL")
        assertEquals("12", viewModel.inputValue.value)

        viewModel.onKeyInput("DEL")
        assertEquals("1", viewModel.inputValue.value)

        viewModel.onKeyInput("DEL")
        assertEquals("0", viewModel.inputValue.value)
    }

    @Test
    fun testKeyInputNegation() {
        viewModel.onKeyInput("C")
        viewModel.onKeyInput("9")
        viewModel.onKeyInput("±")
        assertEquals("-9", viewModel.inputValue.value)

        viewModel.onKeyInput("±")
        assertEquals("9", viewModel.inputValue.value)
    }

    @Test
    fun testSwapUnits() {
        val originalFrom = viewModel.fromUnit.value
        val originalTo = viewModel.toUnit.value
        assertNotEquals(originalFrom.id, originalTo.id)

        viewModel.swapUnits()

        assertEquals(originalTo.id, viewModel.fromUnit.value.id)
        assertEquals(originalFrom.id, viewModel.toUnit.value.id)
    }

    @Test
    fun testCategorySwitching() {
        viewModel.selectCategory(UnitCategory.TEMPERATURE)
        assertEquals(UnitCategory.TEMPERATURE, viewModel.selectedCategory.value)
        assertEquals(UnitCategory.TEMPERATURE, viewModel.fromUnit.value.category)
        assertEquals(UnitCategory.TEMPERATURE, viewModel.toUnit.value.category)

        viewModel.selectCategory(UnitCategory.DIGITAL)
        assertEquals(UnitCategory.DIGITAL, viewModel.selectedCategory.value)
        assertEquals(UnitCategory.DIGITAL, viewModel.fromUnit.value.category)
        assertEquals(UnitCategory.DIGITAL, viewModel.toUnit.value.category)
    }
}
