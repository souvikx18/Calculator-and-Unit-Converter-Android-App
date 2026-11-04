package com.example.domain.converter

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

object UnitConverterEngine {

    val allUnits: List<ConversionUnit> = listOf(
        // Length (Base: Meter)
        ConversionUnit("m", "Meter", "m", UnitCategory.LENGTH, 1.0),
        ConversionUnit("km", "Kilometer", "km", UnitCategory.LENGTH, 1000.0),
        ConversionUnit("cm", "Centimeter", "cm", UnitCategory.LENGTH, 0.01),
        ConversionUnit("mm", "Millimeter", "mm", UnitCategory.LENGTH, 0.001),
        ConversionUnit("um", "Micrometer", "µm", UnitCategory.LENGTH, 1e-6),
        ConversionUnit("nm", "Nanometer", "nm", UnitCategory.LENGTH, 1e-9),
        ConversionUnit("mi", "Mile", "mi", UnitCategory.LENGTH, 1609.344),
        ConversionUnit("yd", "Yard", "yd", UnitCategory.LENGTH, 0.9144),
        ConversionUnit("ft", "Foot", "ft", UnitCategory.LENGTH, 0.3048),
        ConversionUnit("in", "Inch", "in", UnitCategory.LENGTH, 0.0254),
        ConversionUnit("nmi", "Nautical Mile", "NM", UnitCategory.LENGTH, 1852.0),

        // Mass (Base: Kilogram)
        ConversionUnit("kg", "Kilogram", "kg", UnitCategory.MASS, 1.0),
        ConversionUnit("g", "Gram", "g", UnitCategory.MASS, 0.001),
        ConversionUnit("mg", "Milligram", "mg", UnitCategory.MASS, 1e-6),
        ConversionUnit("t", "Metric Ton", "t", UnitCategory.MASS, 1000.0),
        ConversionUnit("lb", "Pound", "lb", UnitCategory.MASS, 0.45359237),
        ConversionUnit("oz", "Ounce", "oz", UnitCategory.MASS, 0.028349523125),
        ConversionUnit("st", "Stone", "st", UnitCategory.MASS, 6.35029318),

        // Temperature (Base: Celsius)
        ConversionUnit("c", "Celsius", "°C", UnitCategory.TEMPERATURE, 1.0, isCustomFormula = true),
        ConversionUnit("f", "Fahrenheit", "°F", UnitCategory.TEMPERATURE, 1.0, isCustomFormula = true),
        ConversionUnit("k", "Kelvin", "K", UnitCategory.TEMPERATURE, 1.0, isCustomFormula = true),
        ConversionUnit("r", "Rankine", "°R", UnitCategory.TEMPERATURE, 1.0, isCustomFormula = true),

        // Speed (Base: m/s)
        ConversionUnit("mps", "Meters per sec", "m/s", UnitCategory.SPEED, 1.0),
        ConversionUnit("kph", "Kilometers per hr", "km/h", UnitCategory.SPEED, 1.0 / 3.6),
        ConversionUnit("mph", "Miles per hour", "mph", UnitCategory.SPEED, 0.44704),
        ConversionUnit("knot", "Knot", "kn", UnitCategory.SPEED, 0.514444444),
        ConversionUnit("fps", "Feet per sec", "ft/s", UnitCategory.SPEED, 0.3048),

        // Area (Base: Square Meter)
        ConversionUnit("sq_m", "Square Meter", "m²", UnitCategory.AREA, 1.0),
        ConversionUnit("sq_km", "Square Kilometer", "km²", UnitCategory.AREA, 1e6),
        ConversionUnit("sq_cm", "Square Centimeter", "cm²", UnitCategory.AREA, 1e-4),
        ConversionUnit("ha", "Hectare", "ha", UnitCategory.AREA, 10000.0),
        ConversionUnit("acre", "Acre", "ac", UnitCategory.AREA, 4046.8564224),
        ConversionUnit("sq_mi", "Square Mile", "mi²", UnitCategory.AREA, 2589988.110336),
        ConversionUnit("sq_yd", "Square Yard", "yd²", UnitCategory.AREA, 0.83612736),
        ConversionUnit("sq_ft", "Square Foot", "ft²", UnitCategory.AREA, 0.09290304),
        ConversionUnit("sq_in", "Square Inch", "in²", UnitCategory.AREA, 0.00064516),

        // Volume (Base: Liter)
        ConversionUnit("l", "Liter", "L", UnitCategory.VOLUME, 1.0),
        ConversionUnit("ml", "Milliliter", "mL", UnitCategory.VOLUME, 0.001),
        ConversionUnit("m3", "Cubic Meter", "m³", UnitCategory.VOLUME, 1000.0),
        ConversionUnit("gal_us", "Gallon (US)", "gal", UnitCategory.VOLUME, 3.785411784),
        ConversionUnit("qt_us", "Quart (US)", "qt", UnitCategory.VOLUME, 0.946352946),
        ConversionUnit("pt_us", "Pint (US)", "pt", UnitCategory.VOLUME, 0.473176473),
        ConversionUnit("cup_us", "Cup (US)", "cup", UnitCategory.VOLUME, 0.2365882365),
        ConversionUnit("fl_oz_us", "Fluid Ounce (US)", "fl oz", UnitCategory.VOLUME, 0.0295735295625),
        ConversionUnit("gal_uk", "Gallon (Imperial)", "gal (UK)", UnitCategory.VOLUME, 4.54609),

        // Digital Data (Base: Byte)
        ConversionUnit("byte", "Byte", "B", UnitCategory.DIGITAL, 1.0),
        ConversionUnit("bit", "Bit", "bit", UnitCategory.DIGITAL, 0.125),
        ConversionUnit("kb", "Kilobyte (decimal)", "KB", UnitCategory.DIGITAL, 1000.0),
        ConversionUnit("mb", "Megabyte (decimal)", "MB", UnitCategory.DIGITAL, 1e6),
        ConversionUnit("gb", "Gigabyte (decimal)", "GB", UnitCategory.DIGITAL, 1e9),
        ConversionUnit("tb", "Terabyte (decimal)", "TB", UnitCategory.DIGITAL, 1e12),
        ConversionUnit("kib", "Kibibyte (binary)", "KiB", UnitCategory.DIGITAL, 1024.0),
        ConversionUnit("mib", "Mebibyte (binary)", "MiB", UnitCategory.DIGITAL, 1048576.0),
        ConversionUnit("gib", "Gibibyte (binary)", "GiB", UnitCategory.DIGITAL, 1073741824.0),

        // Time (Base: Second)
        ConversionUnit("s", "Second", "s", UnitCategory.TIME, 1.0),
        ConversionUnit("ms", "Millisecond", "ms", UnitCategory.TIME, 0.001),
        ConversionUnit("min", "Minute", "min", UnitCategory.TIME, 60.0),
        ConversionUnit("hr", "Hour", "h", UnitCategory.TIME, 3600.0),
        ConversionUnit("day", "Day", "d", UnitCategory.TIME, 86400.0),
        ConversionUnit("wk", "Week", "wk", UnitCategory.TIME, 604800.0),
        ConversionUnit("mo", "Month (avg)", "mo", UnitCategory.TIME, 2629800.0),
        ConversionUnit("yr", "Year (365d)", "yr", UnitCategory.TIME, 31536000.0),

        // Pressure (Base: Pascal)
        ConversionUnit("pa", "Pascal", "Pa", UnitCategory.PRESSURE, 1.0),
        ConversionUnit("kpa", "Kilopascal", "kPa", UnitCategory.PRESSURE, 1000.0),
        ConversionUnit("bar", "Bar", "bar", UnitCategory.PRESSURE, 100000.0),
        ConversionUnit("mbar", "Millibar", "mbar", UnitCategory.PRESSURE, 100.0),
        ConversionUnit("psi", "Pound/sq inch", "psi", UnitCategory.PRESSURE, 6894.75729),
        ConversionUnit("atm", "Atmosphere", "atm", UnitCategory.PRESSURE, 101325.0),
        ConversionUnit("mmhg", "Torr / mmHg", "mmHg", UnitCategory.PRESSURE, 133.322368),

        // Energy (Base: Joule)
        ConversionUnit("j", "Joule", "J", UnitCategory.ENERGY, 1.0),
        ConversionUnit("kj", "Kilojoule", "kJ", UnitCategory.ENERGY, 1000.0),
        ConversionUnit("cal", "Calorie", "cal", UnitCategory.ENERGY, 4.184),
        ConversionUnit("kcal", "Kilocalorie", "kcal", UnitCategory.ENERGY, 4184.0),
        ConversionUnit("wh", "Watt-hour", "Wh", UnitCategory.ENERGY, 3600.0),
        ConversionUnit("kwh", "Kilowatt-hour", "kWh", UnitCategory.ENERGY, 3.6e6),
        ConversionUnit("ev", "Electron-volt", "eV", UnitCategory.ENERGY, 1.602176634e-19),
        ConversionUnit("hp_h", "Horsepower-hour", "hp·h", UnitCategory.ENERGY, 2684519.537)
    )

    fun getUnitsForCategory(category: UnitCategory): List<ConversionUnit> {
        return allUnits.filter { it.category == category }
    }

    fun convert(value: Double, from: ConversionUnit, to: ConversionUnit): Double {
        if (from.id == to.id) return value

        if (from.category == UnitCategory.TEMPERATURE) {
            return convertTemperature(value, from.id, to.id)
        }

        // Standard linear conversion: val * from.factorToBase / to.factorToBase
        val inBase = value * from.factorToBase
        return inBase / to.factorToBase
    }

    private fun convertTemperature(value: Double, fromId: String, toId: String): Double {
        // Step 1: convert to Celsius
        val celsius = when (fromId) {
            "c" -> value
            "f" -> (value - 32.0) * 5.0 / 9.0
            "k" -> value - 273.15
            "r" -> (value - 491.67) * 5.0 / 9.0
            else -> value
        }

        // Step 2: convert Celsius to target
        return when (toId) {
            "c" -> celsius
            "f" -> celsius * 9.0 / 5.0 + 32.0
            "k" -> celsius + 273.15
            "r" -> (celsius + 273.15) * 9.0 / 5.0
            else -> celsius
        }
    }

    fun formatResult(value: Double): String {
        if (value.isNaN()) return "0"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"
        if (abs(value) == 0.0) return "0"

        val absVal = abs(value)
        return if (absVal >= 1e9 || (absVal < 1e-4 && absVal > 0)) {
            val df = DecimalFormat("0.######E0", DecimalFormatSymbols(Locale.US))
            df.format(value)
        } else {
            val bd = BigDecimal(value, MathContext(10, RoundingMode.HALF_UP))
                .setScale(8, RoundingMode.HALF_UP)
                .stripTrailingZeros()
            bd.toPlainString()
        }
    }
}
