package com.example.ui.converter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.preferences.CalculatorPreferences
import com.example.domain.converter.ConversionUnit
import com.example.domain.converter.UnitCategory
import com.example.domain.converter.UnitConverterEngine
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QuickConversionItem(
    val unit: ConversionUnit,
    val valueFormatted: String
)

class ConverterViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = CalculatorPreferences.getInstance(application)
    val hapticEnabled: StateFlow<Boolean> = preferences.hapticFeedback

    private val _selectedCategory = MutableStateFlow(UnitCategory.LENGTH)
    val selectedCategory: StateFlow<UnitCategory> = _selectedCategory.asStateFlow()

    private val _availableUnits = MutableStateFlow(UnitConverterEngine.getUnitsForCategory(UnitCategory.LENGTH))
    val availableUnits: StateFlow<List<ConversionUnit>> = _availableUnits.asStateFlow()

    private val _fromUnit = MutableStateFlow(_availableUnits.value[0])
    val fromUnit: StateFlow<ConversionUnit> = _fromUnit.asStateFlow()

    private val _toUnit = MutableStateFlow(_availableUnits.value[1])
    val toUnit: StateFlow<ConversionUnit> = _toUnit.asStateFlow()

    private val _inputValue = MutableStateFlow("1")
    val inputValue: StateFlow<String> = _inputValue.asStateFlow()

    private val _outputValue = MutableStateFlow("")
    val outputValue: StateFlow<String> = _outputValue.asStateFlow()

    private val _allConvertedList = MutableStateFlow<List<QuickConversionItem>>(emptyList())
    val allConvertedList: StateFlow<List<QuickConversionItem>> = _allConvertedList.asStateFlow()

    init {
        recalculate()
    }

    fun selectCategory(category: UnitCategory) {
        if (_selectedCategory.value == category) return
        _selectedCategory.value = category
        val units = UnitConverterEngine.getUnitsForCategory(category)
        _availableUnits.value = units
        _fromUnit.value = units.firstOrNull() ?: units[0]
        _toUnit.value = if (units.size > 1) units[1] else units[0]
        recalculate()
    }

    fun setFromUnit(unit: ConversionUnit) {
        _fromUnit.value = unit
        recalculate()
    }

    fun setToUnit(unit: ConversionUnit) {
        _toUnit.value = unit
        recalculate()
    }

    fun swapUnits() {
        val temp = _fromUnit.value
        _fromUnit.value = _toUnit.value
        _toUnit.value = temp
        recalculate()
    }

    fun onKeyInput(key: String) {
        val current = _inputValue.value
        when (key) {
            "C" -> _inputValue.value = "0"
            "DEL" -> {
                _inputValue.value = if (current.length <= 1) "0" else current.dropLast(1)
            }
            "±" -> {
                _inputValue.value = if (current.startsWith("-")) current.removePrefix("-") else if (current == "0") "0" else "-$current"
            }
            "00" -> {
                if (current != "0" && current != "-0" && current.length < 14) {
                    _inputValue.value = "${current}00"
                }
            }
            "." -> {
                if (!current.contains(".")) {
                    _inputValue.value = "$current."
                }
            }
            else -> {
                // Digits
                if (current == "0") {
                    _inputValue.value = key
                } else if (current == "-0") {
                    _inputValue.value = "-$key"
                } else if (current.length < 15) {
                    _inputValue.value = "$current$key"
                }
            }
        }
        recalculate()
    }

    private var conversionJob: Job? = null

    private fun recalculate() {
        val inputDouble = _inputValue.value.toDoubleOrNull() ?: 0.0
        val from = _fromUnit.value
        val to = _toUnit.value
        val converted = UnitConverterEngine.convert(inputDouble, from, to)
        _outputValue.value = UnitConverterEngine.formatResult(converted)

        conversionJob?.cancel()
        val currentUnits = _availableUnits.value
        conversionJob = viewModelScope.launch(Dispatchers.Default) {
            val list = currentUnits.map { unit ->
                val res = UnitConverterEngine.convert(inputDouble, from, unit)
                QuickConversionItem(
                    unit = unit,
                    valueFormatted = UnitConverterEngine.formatResult(res)
                )
            }
            _allConvertedList.value = list
        }
    }
}
