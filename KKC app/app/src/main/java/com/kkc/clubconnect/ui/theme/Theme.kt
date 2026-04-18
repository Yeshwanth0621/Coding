package com.kkc.clubconnect.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ClubColors = darkColorScheme(
    primary = Teal,
    onPrimary = Paper,
    secondary = GlowSecondary,
    onSecondary = Ink,
    tertiary = Mint,
    background = Paper,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    surfaceVariant = PanelTop,
    onSurfaceVariant = Slate,
    error = Alert,
)

@Composable
fun ClubConnectTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = ClubColors,
        typography = ClubTypography,
        content = content,
    )
}
