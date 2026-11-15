package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
  primary = HoloNeonCyan,
  onPrimary = CyberObsidianVoid,
  primaryContainer = Color(0xFF09253A),
  onPrimaryContainer = HoloNeonCyan,
  secondary = HoloNeonEmerald,
  onSecondary = CyberObsidianVoid,
  secondaryContainer = Color(0xFF072F21),
  onSecondaryContainer = HoloNeonEmerald,
  tertiary = HoloNeonGold,
  onTertiary = CyberObsidianVoid,
  background = CyberObsidianVoid,
  onBackground = TextDarkPrimary,
  surface = CyberObsidianSurface,
  onSurface = TextDarkPrimary,
  surfaceVariant = CyberObsidianCard,
  onSurfaceVariant = TextDarkSecondary,
  error = NeonCoral,
  onError = Color.White,
  errorContainer = Color(0xFF4C0F1E),
  onErrorContainer = Color(0xFFFFB4C0),
  outline = Color(0xFF1E2B45)
)

private val LightColorScheme = lightColorScheme(
  primary = EmeraldDark,
  onPrimary = Color.White,
  primaryContainer = EmeraldContainerLight,
  onPrimaryContainer = EmeraldDark,
  secondary = GoldAccent,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFFEF3C7),
  onSecondaryContainer = Color(0xFF92400E),
  tertiary = Color(0xFF0284C7),
  onTertiary = Color.White,
  background = Color(0xFFF1F5F9),
  onBackground = TextLightPrimary,
  surface = Color(0xFFFFFFFF),
  onSurface = TextLightPrimary,
  surfaceVariant = Color(0xFFE2E8F0),
  onSurfaceVariant = TextLightSecondary,
  error = LossRed,
  onError = Color.White,
  errorContainer = LossRedLight,
  onErrorContainer = LossRedDark,
  outline = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = colorScheme.background.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
