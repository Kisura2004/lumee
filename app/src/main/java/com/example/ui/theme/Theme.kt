package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = CozyPrimary,
    secondary = CozySecondary,
    tertiary = CozyTertiary,
    background = CozyBackground,
    surface = CozySurface,
    surfaceVariant = CozySurfaceVariant,
    onPrimary = Color(0xFF451A03),     // Deep Brown/Amber contrast
    onSecondary = Color(0xFF450A0A),   // Deep Rose contrast
    onBackground = Color(0xFFF8FAFC),  // Clean Off-White
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFFE2E8F0)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = GlassAccent,                  // Soft rose blush highlight
    secondary = GlassTextSecondary,         // Warm dark slate/grey text
    tertiary = GlassAccent,
    background = GlassBgStart,              // Soft cream base
    surface = GlassCardBackground,          // Frosted translucent surface
    surfaceVariant = GlassCardBackgroundHover,
    onPrimary = Color.White,
    onSecondary = GlassTextPrimary,
    onBackground = GlassTextPrimary,        // Rich Charcoal text
    onSurface = GlassTextPrimary,
    onSurfaceVariant = GlassTextSecondary
  )

@Composable
fun MyApplicationTheme(
  palette: String = "PEACH",
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Force deactivate dynamic system color to preserve our custom premium curated Frosted Glass theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val accentColor = when (palette) {
    "TWILIGHT" -> Color(0xFF9E7FFE)
    "FOREST" -> Color(0xFF4E7A53)
    "OCEAN" -> Color(0xFF3882B6)
    else -> Color(0xFFE0A7A7) // "PEACH"
  }

  val textColor = when (palette) {
    "TWILIGHT" -> Color(0xFFECE5DF)
    else -> Color(0xFF3D3834)
  }

  val subTextColor = when (palette) {
    "TWILIGHT" -> Color(0xFFECE5DF).copy(alpha = 0.7f)
    else -> Color(0xFF423E3B)
  }

  val selectedLightScheme = lightColorScheme(
    primary = accentColor,
    secondary = subTextColor,
    tertiary = accentColor,
    background = when (palette) {
      "TWILIGHT" -> Color(0xFF0F101A)
      "FOREST" -> Color(0xFFF4F9F4)
      "OCEAN" -> Color(0xFFF0F4FF)
      else -> Color(0xFFFDF6F0)
    },
    surface = Color(0x33FFFFFF),
    surfaceVariant = Color(0x4DFFFFFF),
    onPrimary = Color.White,
    onSecondary = textColor,
    onBackground = textColor,
    onSurface = textColor,
    onSurfaceVariant = subTextColor
  )

  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> selectedLightScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

