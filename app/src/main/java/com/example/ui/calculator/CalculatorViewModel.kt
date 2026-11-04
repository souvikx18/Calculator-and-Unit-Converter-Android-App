package com.example.ui.calculator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CalculationDatabase
import com.example.data.local.CalculationHistoryEntity
import com.example.data.preferences.CalculatorPreferences
import com.example.domain.calculator.ExpressionEvaluator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val database = CalculationDatabase.getInstance(application)
    private val calculationDao = database.calculationDao()
    private val preferences = CalculatorPreferences.getInstance(application)
    private val evaluator = ExpressionEvaluator()

    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _previewResult = MutableStateFlow<String?>(null)
    val previewResult: StateFlow<String?> = _previewResult.asStateFlow()

    private val _isDeg = MutableStateFlow(true)
    val isDeg: StateFlow<Boolean> = _isDeg.asStateFlow()

    private val _isInverse = MutableStateFlow(false)
    val isInverse: StateFlow<Boolean> = _isInverse.asStateFlow()

    private val _isScientificExpanded = MutableStateFlow(false)
    val isScientificExpanded: StateFlow<Boolean> = _isScientificExpanded.asStateFlow()

    private val _memoryValue = MutableStateFlow<Double?>(null)
    val memoryValue: StateFlow<Double?> = _memoryValue.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val hapticEnabled: StateFlow<Boolean> = preferences.hapticFeedback

    val historyList: StateFlow<List<CalculationHistoryEntity>> = calculationDao
        .getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            preferences.angleUnitDeg.collect { deg ->
                _isDeg.value = deg
                updatePreview()
            }
        }
    }

    private var hasJustCalculated = false

    private val OPERATOR_CHARS = setOf('+', '-', '−', '–', '×', '÷', '/', '*', '^')
    private val OPERATORS_AND_PARENS = setOf('+', '-', '−', '–', '×', '÷', '/', '*', '^', '(', ',')

    fun onDigit(d: String) {
        _errorMessage.value = null
        if (hasJustCalculated) {
            hasJustCalculated = false
            if (d == ".") {
                _expression.value = "0."
            } else {
                _expression.value = d
            }
            updatePreview()
            return
        }

        val current = _expression.value

        if (d == ".") {
            // Find current number token being typed
            val lastNonDigit = current.indexOfLast { it in OPERATORS_AND_PARENS }
            val currentNumber = if (lastNonDigit >= 0) current.substring(lastNonDigit + 1) else current
            if (currentNumber.contains(".")) {
                return // Prevent multiple decimal points in a single number
            }
            if (current.isEmpty() || current.last() in OPERATORS_AND_PARENS) {
                _expression.value = current + "0."
                updatePreview()
                return
            }
        } else {
            // Handle leading zero: "0" + "5" -> "5", "10 + 0" + "5" -> "10 + 5"
            val lastNonDigit = current.indexOfLast { it in OPERATORS_AND_PARENS }
            val currentNumber = if (lastNonDigit >= 0) current.substring(lastNonDigit + 1) else current
            if (currentNumber == "0") {
                if (d == "0") return // Prevent "00"
                _expression.value = current.dropLast(1) + d
                updatePreview()
                return
            }
        }

        _expression.value = current + d
        updatePreview()
    }

    fun onOperator(op: String) {
        hasJustCalculated = false
        _errorMessage.value = null
        val current = _expression.value

        if (current.isEmpty()) {
            if (op == "−" || op == "-") {
                _expression.value = "−"
            }
            return
        }

        val lastChar = current.last()
        // If last char is already an operator, replace it
        if (lastChar in OPERATOR_CHARS) {
            _expression.value = current.dropLast(1) + op
        } else {
            _expression.value = current + op
        }
        updatePreview()
    }

    fun onFunction(func: String) {
        _errorMessage.value = null
        if (hasJustCalculated) {
            hasJustCalculated = false
            _expression.value = "$func("
            updatePreview()
            return
        }
        val current = _expression.value
        _expression.value = "$current$func("
        updatePreview()
    }

    fun onConstant(constant: String) {
        _errorMessage.value = null
        if (hasJustCalculated) {
            hasJustCalculated = false
            _expression.value = constant
            updatePreview()
            return
        }
        _expression.value = "${_expression.value}$constant"
        updatePreview()
    }

    fun onParenthesis() {
        _errorMessage.value = null
        if (hasJustCalculated) {
            hasJustCalculated = false
            _expression.value = "("
            updatePreview()
            return
        }
        val current = _expression.value
        val openCount = current.count { it == '(' }
        val closeCount = current.count { it == ')' }

        if (current.isNotEmpty()) {
            val last = current.last()
            // If we have unclosed parens and the last char is a digit, constant, or ')', close it!
            if (openCount > closeCount && (last.isDigit() || last == ')' || last == 'π' || last == 'e' || last == '!')) {
                _expression.value = "$current)"
                updatePreview()
                return
            }
        }
        _expression.value = "$current("
        updatePreview()
    }

    fun onPower() {
        onOperator("^")
    }

    fun onSquare() {
        hasJustCalculated = false
        val current = _expression.value
        if (current.isNotEmpty()) {
            _expression.value = "$current^2"
            updatePreview()
        }
    }

    fun onCube() {
        hasJustCalculated = false
        val current = _expression.value
        if (current.isNotEmpty()) {
            _expression.value = "$current^3"
            updatePreview()
        }
    }

    fun onReciprocal() {
        hasJustCalculated = false
        val current = _expression.value
        if (current.isEmpty()) {
            _expression.value = "1/("
        } else {
            _expression.value = "1/($current)"
        }
        updatePreview()
    }

    fun onFactorial() {
        hasJustCalculated = false
        val current = _expression.value
        if (current.isNotEmpty() && (current.last().isDigit() || current.last() == ')')) {
            _expression.value = "$current!"
            updatePreview()
        }
    }

    fun onPercentage() {
        hasJustCalculated = false
        val current = _expression.value
        if (current.isNotEmpty() && (current.last().isDigit() || current.last() == ')')) {
            _expression.value = "$current%"
            updatePreview()
        }
    }

    fun onPlusMinus() {
        hasJustCalculated = false
        val current = _expression.value
        if (current.isEmpty()) {
            _expression.value = "−"
            return
        }

        if (current == "−" || current == "-") {
            _expression.value = ""
            return
        }

        // If it ends with an operator like +− or *−, toggle off the trailing minus
        if ((current.endsWith("−") || current.endsWith("-")) && current.length >= 2 && current[current.length - 2] in OPERATOR_CHARS) {
            _expression.value = current.dropLast(1)
            updatePreview()
            return
        }

        // If it ends with an operator like + or ×, append −
        if (current.last() in OPERATOR_CHARS) {
            _expression.value = current + "−"
            updatePreview()
            return
        }

        // If it ends with a closing parenthesis, toggle sign of the parenthesized group
        if (current.endsWith(")")) {
            var depth = 0
            var openIndex = -1
            for (idx in current.indices.reversed()) {
                if (current[idx] == ')') depth++
                else if (current[idx] == '(') {
                    depth--
                    if (depth == 0) {
                        openIndex = idx
                        break
                    }
                }
            }
            if (openIndex != -1) {
                if (openIndex > 0 && (current[openIndex - 1] == '−' || current[openIndex - 1] == '-')) {
                    val isUnary = openIndex - 1 == 0 || current[openIndex - 2] in OPERATOR_CHARS
                    if (isUnary) {
                        _expression.value = current.substring(0, openIndex - 1) + current.substring(openIndex)
                        updatePreview()
                        return
                    }
                }
                _expression.value = current.substring(0, openIndex) + "−" + current.substring(openIndex)
                updatePreview()
                return
            }
        }

        // Normal number / token at the end
        var i = current.length - 1
        while (i >= 0 && (current[i].isDigit() || current[i] == '.' || current[i] == 'π' || current[i] == 'e' || current[i] == '%')) {
            i--
        }

        if (i >= 0 && (current[i] == '−' || current[i] == '-')) {
            val isUnary = i == 0 || current[i - 1] in OPERATOR_CHARS
            if (isUnary) {
                _expression.value = current.substring(0, i) + current.substring(i + 1)
                updatePreview()
                return
            }
        }

        _expression.value = current.substring(0, i + 1) + "−" + current.substring(i + 1)
        updatePreview()
    }

    fun onBackspace() {
        hasJustCalculated = false
        val current = _expression.value
        if (current.isEmpty()) return

        // Smart multi-char removal for function names like "sin(", "cos(", "asin(", "sqrt(", etc.
        val functionTokens = listOf(
            "asin(", "acos(", "atan(", "sinh(", "cosh(", "tanh(",
            "sin(", "cos(", "tan(", "ln(", "log(", "sqrt(", "cbrt(", "abs("
        )
        for (token in functionTokens) {
            if (current.endsWith(token)) {
                _expression.value = current.dropLast(token.length)
                updatePreview()
                return
            }
        }

        _expression.value = current.dropLast(1)
        updatePreview()
    }

    fun onClear() {
        hasJustCalculated = false
        _expression.value = ""
        _previewResult.value = null
        _errorMessage.value = null
    }

    fun onEquals() {
        val current = _expression.value.trim()
        if (current.isEmpty()) return

        when (val result = evaluator.evaluate(current, _isDeg.value)) {
            is ExpressionEvaluator.EvaluationResult.Success -> {
                val formatted = result.formatted
                val isSci = _isScientificExpanded.value || current.any { it in "sincostanlogln√^!πe" }

                // Save to Room history
                viewModelScope.launch {
                    calculationDao.insert(
                        CalculationHistoryEntity(
                            expression = current,
                            result = formatted,
                            isScientific = isSci
                        )
                    )
                }

                _expression.value = formatted
                _previewResult.value = null
                _errorMessage.value = null
                hasJustCalculated = true
            }
            is ExpressionEvaluator.EvaluationResult.Error -> {
                _errorMessage.value = result.message
                hasJustCalculated = false
            }
        }
    }

    fun onToggleAngleUnit() {
        val newDeg = !_isDeg.value
        _isDeg.value = newDeg
        preferences.setAngleUnitDeg(newDeg)
        updatePreview()
    }

    fun onToggleInverse() {
        _isInverse.value = !_isInverse.value
    }

    fun onToggleScientificPanel() {
        _isScientificExpanded.value = !_isScientificExpanded.value
    }

    // Memory operations
    fun onMemoryClear() {
        _memoryValue.value = null
    }

    fun onMemoryRecall() {
        val mem = _memoryValue.value ?: return
        val formatted = evaluator.formatNumber(mem)
        _expression.value = "${_expression.value}$formatted"
        updatePreview()
    }

    fun onMemoryAdd() {
        val current = _expression.value
        if (current.isEmpty()) return
        val res = evaluator.evaluate(current, _isDeg.value)
        if (res is ExpressionEvaluator.EvaluationResult.Success) {
            val prev = _memoryValue.value ?: 0.0
            _memoryValue.value = prev + res.value
        }
    }

    fun onMemorySubtract() {
        val current = _expression.value
        if (current.isEmpty()) return
        val res = evaluator.evaluate(current, _isDeg.value)
        if (res is ExpressionEvaluator.EvaluationResult.Success) {
            val prev = _memoryValue.value ?: 0.0
            _memoryValue.value = prev - res.value
        }
    }

    fun onMemoryStore() {
        val current = _expression.value
        if (current.isEmpty()) return
        val res = evaluator.evaluate(current, _isDeg.value)
        if (res is ExpressionEvaluator.EvaluationResult.Success) {
            _memoryValue.value = res.value
        }
    }

    fun restoreHistoryItem(item: CalculationHistoryEntity, useResultOnly: Boolean = false) {
        hasJustCalculated = false
        if (useResultOnly) {
            _expression.value = item.result
        } else {
            _expression.value = item.expression
        }
        updatePreview()
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            calculationDao.clearAll()
        }
    }

    fun deleteHistoryItem(item: CalculationHistoryEntity) {
        viewModelScope.launch {
            calculationDao.delete(item)
        }
    }

    private var previewJob: kotlinx.coroutines.Job? = null

    private fun updatePreview() {
        previewJob?.cancel()
        val current = _expression.value
        val deg = _isDeg.value
        if (current.isEmpty()) {
            _previewResult.value = null
            return
        }
        previewJob = viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val result = evaluator.evaluatePreview(current, deg)
            _previewResult.value = result
        }
    }
}
