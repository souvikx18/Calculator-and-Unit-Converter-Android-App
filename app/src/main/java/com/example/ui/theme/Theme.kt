package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.preferences.AppThemeMode

private val AmoledColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color.Black,
    primaryContainer = ElectricCyanContainer,
    onPrimaryContainer = ElectricCyan,
    secondary = VividEmerald,
    onSecondary = Color.Black,
    secondaryContainer = VividEmeraldContainer,
    onSecondaryContainer = VividEmerald,
    tertiary = CoralRed,
    onTertiary = Color.White,
    tertiaryContainer = CoralRedContainer,
    background = AmoledBackground,
    onBackground = Color(0xFFF8FAFC),
    surface = AmoledSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF27272A),
    error = CoralRed,
    onError = Color.White
)

private val CharcoalColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = ElectricCyanContainer,
    onPrimaryContainer = ElectricCyan,
    secondary = VividEmerald,
    onSecondary = Color.Black,
    secondaryContainer = VividEmeraldContainer,
    onSecondaryContainer = VividEmerald,
    tertiary = CoralRed,
    onTertiary = Color.White,
    tertiaryContainer = CoralRedContainer,
    background = CharcoalBackground,
    onBackground = Color(0xFFF8FAFC),
    surface = CharcoalSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = CharcoalSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
    error = CoralRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF059669),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF047857),
    tertiary = Color(0xFFE11D48),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE4E6),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK_CHARCOAL,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()

    val colorScheme: ColorScheme = when (themeMode) {
        AppThemeMode.DARK_AMOLED -> AmoledColorScheme
        AppThemeMode.DARK_CHARCOAL -> CharcoalColorScheme
        AppThemeMode.LIGHT_MINIMAL -> LightColorScheme
        AppThemeMode.SYSTEM -> if (isSystemDark) CharcoalColorScheme else LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
