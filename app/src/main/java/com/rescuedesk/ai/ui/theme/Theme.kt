package com.rescuedesk.ai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// MVP direction (PRD §4.1): light background, dark high-contrast text.
// Dark mode is intentionally not wired yet; a high-contrast appearance
// option arrives with Screen C/Settings (PRD §5.3, §5.13).
private val RescueDeskLightScheme = lightColorScheme(
    primary = Blue,
    onPrimary = CardWhite,
    secondary = DeepBlue,
    onSecondary = CardWhite,
    background = SoftWhite,
    onBackground = NearBlack,
    surface = CardWhite,
    onSurface = NearBlack,
    surfaceVariant = SoftWhite,
    onSurfaceVariant = DarkGray,
    error = DarkRed,
    onError = CardWhite,
    outline = LightGrayBorder
)

@Composable
fun RescueDeskTheme(
    @Suppress("UNUSED_PARAMETER") useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RescueDeskLightScheme,
        typography = RescueDeskTypography,
        content = content
    )
}
