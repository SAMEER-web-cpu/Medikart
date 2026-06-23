package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MedicalGreenPrimary,
    onPrimary = MedicalWhite,
    primaryContainer = MedicalGreenDark,
    secondary = MedicalAccentTeal,
    onSecondary = MedicalWhite,
    background = MedicalDarkBackground,
    surface = MedicalDarkSurface,
    onBackground = MedicalDarkText,
    onSurface = MedicalDarkText,
    error = MedicalErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = MedicalGreenPrimary,
    onPrimary = MedicalWhite,
    primaryContainer = MedicalGreenLight,
    secondary = MedicalAccentTeal,
    onSecondary = MedicalWhite,
    background = MedicalSurfaceLight,
    surface = MedicalWhite,
    onBackground = MedicalLightText,
    onSurface = MedicalLightText,
    error = MedicalErrorRed
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Set dynamic color default to false to force our corporate medical green & white identity
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
