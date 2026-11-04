package com.example

import com.example.domain.converter.ConversionUnit
import com.example.domain.converter.UnitCategory
import com.example.domain.converter.UnitConverterEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UnitConverterTest {

    @Test
    fun testAllCategoriesHaveUnits() {
        for (category in UnitCategory.values()) {
            val units = UnitConverterEngine.getUnitsForCategory(category)
            assertTrue("Category ${category.displayName} should have units", units.isNotEmpty())
            assertTrue("Category ${category.displayName} should have at least 2 units", units.size >= 2)
            // Verify no duplicate IDs within category
            val ids = units.map { it.id }
            assertEquals("Duplicate unit IDs found in ${category.displayName}", ids.size, ids.toSet().size)
        }
    }

    @Test
    fun testIdentityConversions() {
        for (unit in UnitConverterEngine.allUnits) {
            val result = UnitConverterEngine.convert(42.5, unit, unit)
            assertEquals("Self conversion for ${unit.name} should equal original value", 42.5, result, 1e-9)
        }
    }

    @Test
    fun testLengthConversions() {
        val meter = UnitConverterEngine.allUnits.first { it.id == "m" }
        val km = UnitConverterEngine.allUnits.first { it.id == "km" }
        val cm = UnitConverterEngine.allUnits.first { it.id == "cm" }
        val mm = UnitConverterEngine.allUnits.first { it.id == "mm" }
        val mi = UnitConverterEngine.allUnits.first { it.id == "mi" }
        val yd = UnitConverterEngine.allUnits.first { it.id == "yd" }
        val ft = UnitConverterEngine.allUnits.first { it.id == "ft" }
        val inch = UnitConverterEngine.allUnits.first { it.id == "in" }
        val nmi = UnitConverterEngine.allUnits.first { it.id == "nmi" }

        assertEquals(5.0, UnitConverterEngine.convert(5000.0, meter, km), 1e-9)
        assertEquals(5000.0, UnitConverterEngine.convert(5.0, km, meter), 1e-9)
        assertEquals(100.0, UnitConverterEngine.convert(1.0, meter, cm), 1e-9)
        assertEquals(1000.0, UnitConverterEngine.convert(1.0, meter, mm), 1e-9)
        assertEquals(1609.344, UnitConverterEngine.convert(1.0, mi, meter), 1e-6)
        assertEquals(3.0, UnitConverterEngine.convert(1.0, yd, ft), 1e-6)
        assertEquals(12.0, UnitConverterEngine.convert(1.0, ft, inch), 1e-6)
        assertEquals(1852.0, UnitConverterEngine.convert(1.0, nmi, meter), 1e-6)
    }

    @Test
    fun testMassConversions() {
        val kg = UnitConverterEngine.allUnits.first { it.id == "kg" }
        val g = UnitConverterEngine.allUnits.first { it.id == "g" }
        val mg = UnitConverterEngine.allUnits.first { it.id == "mg" }
        val ton = UnitConverterEngine.allUnits.first { it.id == "t" }
        val lb = UnitConverterEngine.allUnits.first { it.id == "lb" }
        val oz = UnitConverterEngine.allUnits.first { it.id == "oz" }
        val stone = UnitConverterEngine.allUnits.first { it.id == "st" }

        assertEquals(1000.0, UnitConverterEngine.convert(1.0, kg, g), 1e-9)
        assertEquals(1000000.0, UnitConverterEngine.convert(1.0, kg, mg), 1e-9)
        assertEquals(1.0, UnitConverterEngine.convert(1000.0, kg, ton), 1e-9)
        assertEquals(16.0, UnitConverterEngine.convert(1.0, lb, oz), 1e-4)
        assertEquals(14.0, UnitConverterEngine.convert(1.0, stone, lb), 1e-4)
    }

    @Test
    fun testTemperatureConversions() {
        val c = UnitConverterEngine.allUnits.first { it.id == "c" }
        val f = UnitConverterEngine.allUnits.first { it.id == "f" }
        val k = UnitConverterEngine.allUnits.first { it.id == "k" }
        val r = UnitConverterEngine.allUnits.first { it.id == "r" }

        // Water freezing point
        assertEquals(32.0, UnitConverterEngine.convert(0.0, c, f), 1e-6)
        assertEquals(0.0, UnitConverterEngine.convert(32.0, f, c), 1e-6)
        assertEquals(273.15, UnitConverterEngine.convert(0.0, c, k), 1e-6)
        assertEquals(491.67, UnitConverterEngine.convert(0.0, c, r), 1e-2)

        // Water boiling point
        assertEquals(212.0, UnitConverterEngine.convert(100.0, c, f), 1e-6)
        assertEquals(100.0, UnitConverterEngine.convert(212.0, f, c), 1e-6)
        assertEquals(373.15, UnitConverterEngine.convert(100.0, c, k), 1e-6)

        // Absolute zero
        assertEquals(-273.15, UnitConverterEngine.convert(0.0, k, c), 1e-6)
        assertEquals(0.0, UnitConverterEngine.convert(-273.15, c, k), 1e-6)
    }

    @Test
    fun testSpeedConversions() {
        val mps = UnitConverterEngine.allUnits.first { it.id == "mps" }
        val kph = UnitConverterEngine.allUnits.first { it.id == "kph" }
        val mph = UnitConverterEngine.allUnits.first { it.id == "mph" }
        val knot = UnitConverterEngine.allUnits.first { it.id == "knot" }

        assertEquals(36.0, UnitConverterEngine.convert(10.0, mps, kph), 1e-5)
        assertEquals(10.0, UnitConverterEngine.convert(36.0, kph, mps), 1e-5)
        assertEquals(62.1371, UnitConverterEngine.convert(100.0, kph, mph), 1e-3)
        assertEquals(1.94384, UnitConverterEngine.convert(1.0, mps, knot), 1e-3)
    }

    @Test
    fun testAreaConversions() {
        val sqM = UnitConverterEngine.allUnits.first { it.id == "sq_m" }
        val sqKm = UnitConverterEngine.allUnits.first { it.id == "sq_km" }
        val ha = UnitConverterEngine.allUnits.first { it.id == "ha" }
        val acre = UnitConverterEngine.allUnits.first { it.id == "acre" }
        val sqFt = UnitConverterEngine.allUnits.first { it.id == "sq_ft" }

        assertEquals(100.0, UnitConverterEngine.convert(1.0, sqKm, ha), 1e-5)
        assertEquals(10000.0, UnitConverterEngine.convert(1.0, ha, sqM), 1e-5)
        assertEquals(43560.0, UnitConverterEngine.convert(1.0, acre, sqFt), 1e-1)
    }

    @Test
    fun testVolumeConversions() {
        val l = UnitConverterEngine.allUnits.first { it.id == "l" }
        val ml = UnitConverterEngine.allUnits.first { it.id == "ml" }
        val m3 = UnitConverterEngine.allUnits.first { it.id == "m3" }
        val gal = UnitConverterEngine.allUnits.first { it.id == "gal_us" }
        val qt = UnitConverterEngine.allUnits.first { it.id == "qt_us" }
        val cup = UnitConverterEngine.allUnits.first { it.id == "cup_us" }

        assertEquals(1000.0, UnitConverterEngine.convert(1.0, l, ml), 1e-9)
        assertEquals(1000.0, UnitConverterEngine.convert(1.0, m3, l), 1e-9)
        assertEquals(4.0, UnitConverterEngine.convert(1.0, gal, qt), 1e-4)
        assertEquals(16.0, UnitConverterEngine.convert(1.0, gal, cup), 1e-4)
    }

    @Test
    fun testDigitalStorageConversions() {
        val byte = UnitConverterEngine.allUnits.first { it.id == "byte" }
        val bit = UnitConverterEngine.allUnits.first { it.id == "bit" }
        val kb = UnitConverterEngine.allUnits.first { it.id == "kb" }
        val mb = UnitConverterEngine.allUnits.first { it.id == "mb" }
        val gb = UnitConverterEngine.allUnits.first { it.id == "gb" }
        val kib = UnitConverterEngine.allUnits.first { it.id == "kib" }
        val mib = UnitConverterEngine.allUnits.first { it.id == "mib" }
        val gib = UnitConverterEngine.allUnits.first { it.id == "gib" }

        assertEquals(8.0, UnitConverterEngine.convert(1.0, byte, bit), 1e-9)
        assertEquals(1000.0, UnitConverterEngine.convert(1.0, mb, kb), 1e-9)
        assertEquals(1000.0, UnitConverterEngine.convert(1.0, gb, mb), 1e-9)
        assertEquals(1024.0, UnitConverterEngine.convert(1.0, kib, byte), 1e-9)
        assertEquals(1024.0, UnitConverterEngine.convert(1.0, gib, mib), 1e-9)
    }

    @Test
    fun testTimeConversions() {
        val s = UnitConverterEngine.allUnits.first { it.id == "s" }
        val ms = UnitConverterEngine.allUnits.first { it.id == "ms" }
        val min = UnitConverterEngine.allUnits.first { it.id == "min" }
        val hr = UnitConverterEngine.allUnits.first { it.id == "hr" }
        val day = UnitConverterEngine.allUnits.first { it.id == "day" }
        val wk = UnitConverterEngine.allUnits.first { it.id == "wk" }

        assertEquals(1000.0, UnitConverterEngine.convert(1.0, s, ms), 1e-9)
        assertEquals(60.0, UnitConverterEngine.convert(1.0, min, s), 1e-9)
        assertEquals(60.0, UnitConverterEngine.convert(1.0, hr, min), 1e-9)
        assertEquals(24.0, UnitConverterEngine.convert(1.0, day, hr), 1e-9)
        assertEquals(7.0, UnitConverterEngine.convert(1.0, wk, day), 1e-9)
    }

    @Test
    fun testPressureConversions() {
        val pa = UnitConverterEngine.allUnits.first { it.id == "pa" }
        val kpa = UnitConverterEngine.allUnits.first { it.id == "kpa" }
        val bar = UnitConverterEngine.allUnits.first { it.id == "bar" }
        val psi = UnitConverterEngine.allUnits.first { it.id == "psi" }
        val atm = UnitConverterEngine.allUnits.first { it.id == "atm" }

        assertEquals(1000.0, UnitConverterEngine.convert(1.0, kpa, pa), 1e-9)
        assertEquals(100.0, UnitConverterEngine.convert(1.0, bar, kpa), 1e-9)
        assertEquals(101325.0, UnitConverterEngine.convert(1.0, atm, pa), 1e-4)
        assertEquals(14.6959, UnitConverterEngine.convert(1.0, atm, psi), 1e-3)
    }

    @Test
    fun testEnergyConversions() {
        val j = UnitConverterEngine.allUnits.first { it.id == "j" }
        val kj = UnitConverterEngine.allUnits.first { it.id == "kj" }
        val cal = UnitConverterEngine.allUnits.first { it.id == "cal" }
        val kcal = UnitConverterEngine.allUnits.first { it.id == "kcal" }
        val kwh = UnitConverterEngine.allUnits.first { it.id == "kwh" }

        assertEquals(1000.0, UnitConverterEngine.convert(1.0, kj, j), 1e-9)
        assertEquals(4.184, UnitConverterEngine.convert(1.0, cal, j), 1e-5)
        assertEquals(1000.0, UnitConverterEngine.convert(1.0, kcal, cal), 1e-5)
        assertEquals(3600000.0, UnitConverterEngine.convert(1.0, kwh, j), 1e-3)
    }

    @Test
    fun testFormattingResults() {
        assertEquals("0", UnitConverterEngine.formatResult(0.0))
        assertEquals("0", UnitConverterEngine.formatResult(-0.0))
        assertEquals("100", UnitConverterEngine.formatResult(100.0))
        assertEquals("0.5", UnitConverterEngine.formatResult(0.5))
        assertEquals("12.3456", UnitConverterEngine.formatResult(12.3456))
        
        // Large number format check
        val largeFormatted = UnitConverterEngine.formatResult(1e12)
        assertTrue(largeFormatted.contains("E") || largeFormatted.contains("e"))

        // Tiny number format check
        val tinyFormatted = UnitConverterEngine.formatResult(1e-9)
        assertTrue(tinyFormatted.contains("E") || tinyFormatted.contains("e"))
    }
}
