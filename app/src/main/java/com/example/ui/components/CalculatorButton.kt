package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CoralRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.VividEmerald

enum class CalcButtonType {
    NUMBER,
    OPERATOR,
    ACTION,
    CLEAR,
    EQUALS,
    SCIENTIFIC,
    MEMORY
}

@Composable
fun CalculatorButton(
    modifier: Modifier = Modifier,
    text: String = "",
    icon: ImageVector? = null,
    type: CalcButtonType = CalcButtonType.NUMBER,
    testTag: String,
    fontSize: TextUnit = 24.sp,
    cornerRadius: Dp = 18.dp,
    hapticEnabled: Boolean = true,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    val (bgColor, contentColor, fontWeight) = when (type) {
        CalcButtonType.NUMBER -> Triple(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
            MaterialTheme.colorScheme.onSurface,
            FontWeight.Medium
        )
        CalcButtonType.OPERATOR -> Triple(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            ElectricCyan,
            FontWeight.SemiBold
        )
        CalcButtonType.ACTION -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            FontWeight.Normal
        )
        CalcButtonType.CLEAR -> Triple(
            CoralRed.copy(alpha = 0.18f),
            CoralRed,
            FontWeight.SemiBold
        )
        CalcButtonType.EQUALS -> Triple(
            VividEmerald,
            Color.Black,
            FontWeight.Bold
        )
        CalcButtonType.SCIENTIFIC -> Triple(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            MaterialTheme.colorScheme.onSurfaceVariant,
            FontWeight.Normal
        )
        CalcButtonType.MEMORY -> Triple(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
            FontWeight.Medium
        )
    }

    Surface(
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .testTag(testTag)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = contentColor),
                onClick = {
                    if (hapticEnabled) {
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        } catch (_: Exception) {}
                    }
                    onClick()
                }
            ),
        shape = RoundedCornerShape(cornerRadius),
        color = bgColor,
        tonalElevation = 1.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = text.ifEmpty { "button_icon" },
                    tint = contentColor
                )
            } else {
                Text(
                    text = text,
                    color = contentColor,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    maxLines = 1
                )
            }
        }
    }
}
