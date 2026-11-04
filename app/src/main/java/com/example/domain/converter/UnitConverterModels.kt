package com.example.domain.converter

enum class UnitCategory(val displayName: String, val iconName: String) {
    LENGTH("Length", "Straighten"),
    MASS("Mass & Weight", "FitnessCenter"),
    TEMPERATURE("Temperature", "DeviceThermostat"),
    SPEED("Speed", "Speed"),
    AREA("Area", "SquareFoot"),
    VOLUME("Volume", "LocalDrink"),
    DIGITAL("Digital Data", "Memory"),
    TIME("Time", "Schedule"),
    PRESSURE("Pressure", "Compress"),
    ENERGY("Energy", "Bolt")
}

data class ConversionUnit(
    val id: String,
    val name: String,
    val symbol: String,
    val category: UnitCategory,
    // Factor relative to standard base unit in this category (valueInBase = value * factor)
    val factorToBase: Double = 1.0,
    val isCustomFormula: Boolean = false
)
