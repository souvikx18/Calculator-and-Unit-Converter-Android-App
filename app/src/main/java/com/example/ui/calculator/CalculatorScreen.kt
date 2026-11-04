package com.example.ui.calculator

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CalcButtonType
import com.example.ui.components.CalculatorButton
import com.example.ui.components.DisplaySection
import com.example.ui.theme.CoralRed
import com.example.ui.theme.ElectricCyan

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val expression by viewModel.expression.collectAsState()
    val previewResult by viewModel.previewResult.collectAsState()
    val isDeg by viewModel.isDeg.collectAsState()
    val isInverse by viewModel.isInverse.collectAsState()
    val isSciExpanded by viewModel.isScientificExpanded.collectAsState()
    val memoryValue by viewModel.memoryValue.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val hapticEnabled by viewModel.hapticEnabled.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val availableHeight = maxHeight
        val availableWidth = maxWidth

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isLandscape) 12.dp else 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Display Section
            DisplaySection(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (isLandscape) 0.35f else 0.32f),
                expression = expression,
                previewResult = previewResult,
                isDeg = isDeg,
                hasMemory = memoryValue != null,
                isInverse = isInverse,
                onToggleAngleUnit = viewModel::onToggleAngleUnit,
                onBackspace = viewModel::onBackspace,
                onClearAll = viewModel::onClear
            )

            // Memory & Scientific Quick Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Memory buttons strip
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MemoryQuickButton(
                        text = "MC",
                        enabled = memoryValue != null,
                        testTag = "btn_mc",
                        onClick = viewModel::onMemoryClear
                    )
                    MemoryQuickButton(
                        text = "MR",
                        enabled = memoryValue != null,
                        testTag = "btn_mr",
                        onClick = viewModel::onMemoryRecall
                    )
                    MemoryQuickButton(
                        text = "M+",
                        enabled = true,
                        testTag = "btn_m_plus",
                        onClick = viewModel::onMemoryAdd
                    )
                    MemoryQuickButton(
                        text = "M−",
                        enabled = true,
                        testTag = "btn_m_minus",
                        onClick = viewModel::onMemorySubtract
                    )
                }

                // Controls: Scientific Drawer Toggle & History
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = isSciExpanded || isLandscape,
                        onClick = viewModel::onToggleScientificPanel,
                        label = {
                            Text(
                                text = "Fx",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Functions,
                                contentDescription = "Scientific panel toggle",
                                tint = if (isSciExpanded || isLandscape) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            selectedLabelColor = ElectricCyan
                        ),
                        modifier = Modifier.testTag("toggle_scientific_chip")
                    )

                    IconButton(
                        onClick = onOpenHistory,
                        modifier = Modifier.testTag("open_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Calculation history",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Keypad Layout (Adaptive between Portrait and Landscape)
            if (isLandscape) {
                // Landscape: Side-by-side scientific and standard keys
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.65f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Scientific keys column
                    ScientificKeypadGrid(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        viewModel = viewModel,
                        isInverse = isInverse,
                        hapticEnabled = hapticEnabled
                    )

                    // Standard keypad column
                    StandardKeypadGrid(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight(),
                        viewModel = viewModel,
                        hapticEnabled = hapticEnabled
                    )
                }
            } else {
                // Portrait: Vertical stack with collapsible scientific panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.68f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AnimatedVisibility(
                        visible = isSciExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        ScientificKeypadGrid(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            viewModel = viewModel,
                            isInverse = isInverse,
                            hapticEnabled = hapticEnabled
                        )
                    }

                    StandardKeypadGrid(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        viewModel = viewModel,
                        hapticEnabled = hapticEnabled
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }
}

@Composable
private fun MemoryQuickButton(
    text: String,
    enabled: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    CalculatorButton(
        text = text,
        type = CalcButtonType.MEMORY,
        testTag = testTag,
        fontSize = 12.sp,
        cornerRadius = 10.dp,
        modifier = Modifier
            .width(42.dp)
            .height(32.dp),
        onClick = { if (enabled) onClick() }
    )
}

@Composable
private fun ScientificKeypadGrid(
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel,
    isInverse: Boolean,
    hapticEnabled: Boolean
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1: 2nd, sin/asin, cos/acos, tan/atan, e^x / ln
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorButton(
                text = "2nd",
                type = if (isInverse) CalcButtonType.OPERATOR else CalcButtonType.SCIENTIFIC,
                testTag = "btn_inverse",
                fontSize = 14.sp,
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = viewModel::onToggleInverse
            )
            CalculatorButton(
                text = if (isInverse) "asin" else "sin",
                type = CalcButtonType.SCIENTIFIC,
                testTag = "btn_sin",
                fontSize = 14.sp,
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onFunction(if (isInverse) "asin" else "sin") }
            )
            CalculatorButton(
                text = if (isInverse) "acos" else "cos",
                type = CalcButtonType.SCIENTIFIC,
                testTag = "btn_cos",
                fontSize = 14.sp,
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onFunction(if (isInverse) "acos" else "cos") }
            )
            CalculatorButton(
                text = if (isInverse) "atan" else "tan",
                type = CalcButtonType.SCIENTIFIC,
                testTag = "btn_tan",
                fontSize = 14.sp,
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onFunction(if (isInverse) "atan" else "tan") }
            )
            CalculatorButton(
                text = if (isInverse) "eˣ" else "ln",
                type = CalcButtonType.SCIENTIFIC,
                testTag = "btn_ln",
                fontSize = 14.sp,
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = {
                    if (isInverse) {
                        viewModel.onDigit("e")
                        viewModel.onPower()
                    } else {
                        viewModel.onFunction("ln")
                    }
                }
            )
        }

        // Row 2: log/10^x, x^y, √/x², (, )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorButton(
                text = if (isInverse) "10ˣ" else "log",
                type = CalcButtonType.SCIENTIFIC,
                testTag = "btn_log",
                fontSize = 14.sp,
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = {
                    if (isInverse) {
                        viewModel.onDigit("10")
                        viewModel.onPower()
                    } else {
                        viewModel.onFunction("log")
                    }
                }
            )
            CalculatorButton(
                text = "xʸ",
                type = CalcButtonType.SCIENTIFIC,
                testTag = "btn_pow",
                fontSize = 14.sp,
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = viewModel::onPower
            )
            CalculatorButton(
                text = if (isInverse) "x²" else "√",
                type = CalcButtonType.SCIENTIFIC,
                testTag = "btn_sqrt",
                fontSize = 14.sp,
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = {
                    if (isInverse) viewModel.onSquare() else viewModel.onFunction("sqrt")
                }
            )
            CalculatorButton(
                text = "(",
                type = CalcButtonType.SCIENTIFIC,
                testTag = "btn_paren_open",
                fontSize = 15.sp,
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onDigit("(") }
            )
            CalculatorButton(
                text = ")",
                type = CalcButtonType.SCIENTIFIC,
                testTag = "btn_paren_close",
                fontSize = 15.sp,
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onDigit(")") }
            )
        }

        // Row 3: π, e, n!, 1/x, |x|
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorButton(
                text = "π",
                type = CalcButtonType.SCIENTIFIC,
                testTag = "btn_pi",
                fontSize = 15.sp,
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onConstant("π") }
            )
            CalculatorButton(
                text = "e",
                type = CalcButtonType.SCIENTIFIC,
                testTag = "btn_e",
                fontSize = 15.sp,
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onConstant("e") }
            )
            CalculatorButton(
                text = "n!",
                type = CalcButtonType.SCIENTIFIC,
                testTag = "btn_fact",
                fontSize = 14.sp,
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = viewModel::onFactorial
            )
            CalculatorButton(
                text = "1/x",
                type = CalcButtonType.SCIENTIFIC,
                testTag = "btn_reciprocal",
                fontSize = 14.sp,
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = viewModel::onReciprocal
            )
            CalculatorButton(
                text = "|x|",
                type = CalcButtonType.SCIENTIFIC,
                testTag = "btn_abs",
                fontSize = 14.sp,
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onFunction("abs") }
            )
        }
    }
}

@Composable
private fun StandardKeypadGrid(
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel,
    hapticEnabled: Boolean
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1: C, ( ), %, ÷
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorButton(
                text = "C",
                type = CalcButtonType.CLEAR,
                testTag = "btn_clear",
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = viewModel::onClear
            )
            CalculatorButton(
                text = "( )",
                type = CalcButtonType.ACTION,
                testTag = "btn_smart_paren",
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = viewModel::onParenthesis
            )
            CalculatorButton(
                text = "%",
                type = CalcButtonType.ACTION,
                testTag = "btn_percent",
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = viewModel::onPercentage
            )
            CalculatorButton(
                text = "÷",
                type = CalcButtonType.OPERATOR,
                testTag = "btn_divide",
                fontSize = 26.sp,
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onOperator("÷") }
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorButton(
                text = "7",
                type = CalcButtonType.NUMBER,
                testTag = "btn_7",
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onDigit("7") }
            )
            CalculatorButton(
                text = "8",
                type = CalcButtonType.NUMBER,
                testTag = "btn_8",
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onDigit("8") }
            )
            CalculatorButton(
                text = "9",
                type = CalcButtonType.NUMBER,
                testTag = "btn_9",
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onDigit("9") }
            )
            CalculatorButton(
                text = "×",
                type = CalcButtonType.OPERATOR,
                testTag = "btn_multiply",
                fontSize = 26.sp,
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onOperator("×") }
            )
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorButton(
                text = "4",
                type = CalcButtonType.NUMBER,
                testTag = "btn_4",
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onDigit("4") }
            )
            CalculatorButton(
                text = "5",
                type = CalcButtonType.NUMBER,
                testTag = "btn_5",
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onDigit("5") }
            )
            CalculatorButton(
                text = "6",
                type = CalcButtonType.NUMBER,
                testTag = "btn_6",
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onDigit("6") }
            )
            CalculatorButton(
                text = "−",
                type = CalcButtonType.OPERATOR,
                testTag = "btn_subtract",
                fontSize = 26.sp,
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onOperator("−") }
            )
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorButton(
                text = "1",
                type = CalcButtonType.NUMBER,
                testTag = "btn_1",
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onDigit("1") }
            )
            CalculatorButton(
                text = "2",
                type = CalcButtonType.NUMBER,
                testTag = "btn_2",
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onDigit("2") }
            )
            CalculatorButton(
                text = "3",
                type = CalcButtonType.NUMBER,
                testTag = "btn_3",
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onDigit("3") }
            )
            CalculatorButton(
                text = "+",
                type = CalcButtonType.OPERATOR,
                testTag = "btn_add",
                fontSize = 26.sp,
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onOperator("+") }
            )
        }

        // Row 5: ±, 0, ., =
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorButton(
                text = "±",
                type = CalcButtonType.ACTION,
                testTag = "btn_negate",
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = viewModel::onPlusMinus
            )
            CalculatorButton(
                text = "0",
                type = CalcButtonType.NUMBER,
                testTag = "btn_0",
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onDigit("0") }
            )
            CalculatorButton(
                text = ".",
                type = CalcButtonType.NUMBER,
                testTag = "btn_dot",
                fontSize = 26.sp,
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onDigit(".") }
            )
            CalculatorButton(
                text = "=",
                type = CalcButtonType.EQUALS,
                testTag = "btn_equals",
                fontSize = 28.sp,
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = viewModel::onEquals
            )
        }
    }
}
