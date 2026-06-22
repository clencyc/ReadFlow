package com.example.readflow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color(0xFF000000),
    primaryContainer = PrimaryDark,
    onPrimaryContainer = PrimaryLight,

    secondary = Accent,
    onSecondary = Color(0xFF000000),
    secondaryContainer = AccentLight,
    onSecondaryContainer = Color(0xFF000000),

    tertiary = WarningColor,
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFFffe0b2),
    onTertiaryContainer = Color(0xFF000000),

    background = Background,
    onBackground = TextPrimary,

    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,

    error = ErrorColor,
    onError = Color(0xFFffffff),
    errorContainer = Color(0xFF93000a),
    onErrorContainer = ErrorColor,

    outline = TextTertiary,
    outlineVariant = DividerColor,
    scrim = Color(0xFF000000)
)

@Composable
fun ReadFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
