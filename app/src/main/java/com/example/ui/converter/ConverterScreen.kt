package com.example.ui.converter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.converter.ConversionUnit
import com.example.domain.converter.UnitCategory
import com.example.ui.components.CalcButtonType
import com.example.ui.components.CalculatorButton
import com.example.ui.theme.CoralRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.VividEmerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    viewModel: ConverterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val availableUnits by viewModel.availableUnits.collectAsState()
    val fromUnit by viewModel.fromUnit.collectAsState()
    val toUnit by viewModel.toUnit.collectAsState()
    val inputValue by viewModel.inputValue.collectAsState()
    val outputValue by viewModel.outputValue.collectAsState()
    val allConvertedList by viewModel.allConvertedList.collectAsState()
    val hapticEnabled by viewModel.hapticEnabled.collectAsState()

    var pickingFor: String? by remember { mutableStateOf(null) } // "from" or "to"
    val sheetState = rememberModalBottomSheetState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Horizontal Category Chips
        val chipScrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(chipScrollState)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UnitCategory.values().forEach { category ->
                val isSelected = category == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectCategory(category) },
                    label = {
                        Text(
                            text = category.displayName,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        selectedLabelColor = ElectricCyan
                    ),
                    modifier = Modifier.testTag("category_chip_${category.name.lowercase()}")
                )
            }
        }

        // Conversion Cards & Swap Button Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "From" Unit Card
                UnitInputCard(
                    title = "From",
                    unit = fromUnit,
                    valueText = inputValue,
                    isInput = true,
                    testTag = "from_unit_card",
                    onSelectUnit = { pickingFor = "from" }
                )

                // "To" Unit Card
                UnitInputCard(
                    title = "To",
                    unit = toUnit,
                    valueText = outputValue,
                    isInput = false,
                    testTag = "to_unit_card",
                    onSelectUnit = { pickingFor = "to" },
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Converted Value", "$outputValue ${toUnit.symbol}")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied $outputValue ${toUnit.symbol}", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Central Swap Button
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 6.dp,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("swap_units_button")
                    .clickable { viewModel.swapUnits() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap units",
                        tint = ElectricCyan
                    )
                }
            }
        }

        // Custom Numeric Pad for instant entry
        ConverterKeypad(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
            hapticEnabled = hapticEnabled,
            onKeyPress = viewModel::onKeyInput
        )

        // All Other Units Quick Reference Table
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Text(
                    text = "All ${selectedCategory.displayName} Units",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(allConvertedList) { item ->
                        val isCurrentFrom = item.unit.id == fromUnit.id
                        val isCurrentTo = item.unit.id == toUnit.id

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = item.unit.name,
                                    color = if (isCurrentTo) ElectricCyan else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp,
                                    fontWeight = if (isCurrentTo || isCurrentFrom) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "(${item.unit.symbol})",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }

                            Text(
                                text = item.valueFormatted,
                                color = if (isCurrentTo) ElectricCyan else MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.End
                            )
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for selecting unit
    if (pickingFor != null) {
        ModalBottomSheet(
            onDismissRequest = { pickingFor = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select ${if (pickingFor == "from") "Source" else "Target"} Unit",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableUnits) { unit ->
                        val isSelected = if (pickingFor == "from") unit.id == fromUnit.id else unit.id == toUnit.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (pickingFor == "from") {
                                        viewModel.setFromUnit(unit)
                                    } else {
                                        viewModel.setToUnit(unit)
                                    }
                                    pickingFor = null
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = unit.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Symbol: ${unit.symbol}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = ElectricCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnitInputCard(
    title: String,
    unit: ConversionUnit,
    valueText: String,
    isInput: Boolean,
    testTag: String,
    onSelectUnit: () -> Unit,
    onCopy: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Row: Title & Unit Selector Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .clickable(onClick = onSelectUnit)
                        .testTag("${testTag}_selector")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${unit.name} (${unit.symbol})",
                            color = ElectricCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select unit",
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Value Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = valueText.ifEmpty { "0" },
                    color = if (isInput) MaterialTheme.colorScheme.onSurface else VividEmerald,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                if (onCopy != null) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy result",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConverterKeypad(
    modifier: Modifier = Modifier,
    hapticEnabled: Boolean,
    onKeyPress: (String) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // Row 1: 7, 8, 9, DEL
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            ConverterKey("7", hapticEnabled, modifier = Modifier.weight(1f), onKeyPress)
            ConverterKey("8", hapticEnabled, modifier = Modifier.weight(1f), onKeyPress)
            ConverterKey("9", hapticEnabled, modifier = Modifier.weight(1f), onKeyPress)
            CalculatorButton(
                icon = Icons.AutoMirrored.Filled.Backspace,
                type = CalcButtonType.ACTION,
                testTag = "conv_del",
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { onKeyPress("DEL") }
            )
        }

        // Row 2: 4, 5, 6, C
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            ConverterKey("4", hapticEnabled, modifier = Modifier.weight(1f), onKeyPress)
            ConverterKey("5", hapticEnabled, modifier = Modifier.weight(1f), onKeyPress)
            ConverterKey("6", hapticEnabled, modifier = Modifier.weight(1f), onKeyPress)
            CalculatorButton(
                text = "C",
                type = CalcButtonType.CLEAR,
                testTag = "conv_clear",
                hapticEnabled = hapticEnabled,
                modifier = Modifier.weight(1f),
                onClick = { onKeyPress("C") }
            )
        }

        // Row 3: 1, 2, 3, ±
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            ConverterKey("1", hapticEnabled, modifier = Modifier.weight(1f), onKeyPress)
            ConverterKey("2", hapticEnabled, modifier = Modifier.weight(1f), onKeyPress)
            ConverterKey("3", hapticEnabled, modifier = Modifier.weight(1f), onKeyPress)
            ConverterKey("±", hapticEnabled, modifier = Modifier.weight(1f), onKeyPress)
        }

        // Row 4: 0, 00, .
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            ConverterKey("0", hapticEnabled, modifier = Modifier.weight(2f), onKeyPress)
            ConverterKey("00", hapticEnabled, modifier = Modifier.weight(1f), onKeyPress)
            ConverterKey(".", hapticEnabled, modifier = Modifier.weight(1f), onKeyPress)
        }
    }
}

@Composable
private fun ConverterKey(
    label: String,
    hapticEnabled: Boolean,
    modifier: Modifier = Modifier,
    onKeyPress: (String) -> Unit
) {
    CalculatorButton(
        text = label,
        type = if (label == "±") CalcButtonType.ACTION else CalcButtonType.NUMBER,
        testTag = "conv_key_$label",
        fontSize = 20.sp,
        cornerRadius = 14.dp,
        hapticEnabled = hapticEnabled,
        modifier = modifier,
        onClick = { onKeyPress(label) }
    )
}
