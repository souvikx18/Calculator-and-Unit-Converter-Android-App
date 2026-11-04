package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.data.preferences.CalculatorPreferences
import com.example.ui.MainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferences = CalculatorPreferences.getInstance(this)

        setContent {
            val themeMode by preferences.themeMode.collectAsState()

            MyApplicationTheme(themeMode = themeMode) {
                MainScreen(preferences = preferences)
            }
        }
    }
}
