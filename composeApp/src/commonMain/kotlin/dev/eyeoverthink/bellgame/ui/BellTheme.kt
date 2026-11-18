package dev.eyeoverthink.bellgame.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Dark theme for every target. There is no light variant: the exhibit is a
 * night lab on purpose.
 */
@Composable
fun BellTheme(content: @Composable () -> Unit) {
    val scheme = darkColorScheme(
        primary = BellColors.Ion,
        onPrimary = BellColors.Night,
        secondary = BellColors.Brass,
        onSecondary = BellColors.Night,
        tertiary = BellColors.Alarm,
        onTertiary = BellColors.Night,
        background = BellColors.Night,
        onBackground = BellColors.Ink,
        surface = BellColors.Panel,
        onSurface = BellColors.Ink,
        surfaceVariant = BellColors.Line,
        onSurfaceVariant = BellColors.Muted,
        error = BellColors.Miss,
        onError = BellColors.Night,
    )
    MaterialTheme(
        colorScheme = scheme,
        typography = Typography(),
        content = content,
    )
}
