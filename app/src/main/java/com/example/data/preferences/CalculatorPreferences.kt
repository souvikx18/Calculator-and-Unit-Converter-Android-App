package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    DARK_AMOLED,
    DARK_CHARCOAL,
    LIGHT_MINIMAL,
    SYSTEM
}

class CalculatorPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("omni_calc_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _hapticFeedback = MutableStateFlow(prefs.getBoolean(KEY_HAPTIC, true))
    val hapticFeedback: StateFlow<Boolean> = _hapticFeedback.asStateFlow()

    private val _angleUnitDeg = MutableStateFlow(prefs.getBoolean(KEY_ANGLE_DEG, true))
    val angleUnitDeg: StateFlow<Boolean> = _angleUnitDeg.asStateFlow()

    private fun loadThemeMode(): AppThemeMode {
        val name = prefs.getString(KEY_THEME, AppThemeMode.DARK_CHARCOAL.name) ?: AppThemeMode.DARK_CHARCOAL.name
        return try {
            AppThemeMode.valueOf(name)
        } catch (_: Exception) {
            AppThemeMode.DARK_CHARCOAL
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.name).apply()
        _themeMode.value = mode
    }

    fun setHapticFeedback(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply()
        _hapticFeedback.value = enabled
    }

    fun setAngleUnitDeg(isDeg: Boolean) {
        prefs.edit().putBoolean(KEY_ANGLE_DEG, isDeg).apply()
        _angleUnitDeg.value = isDeg
    }

    companion object {
        private const val KEY_THEME = "key_theme_mode"
        private const val KEY_HAPTIC = "key_haptic_feedback"
        private const val KEY_ANGLE_DEG = "key_angle_deg"

        @Volatile
        private var INSTANCE: CalculatorPreferences? = null

        fun getInstance(context: Context): CalculatorPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = CalculatorPreferences(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
