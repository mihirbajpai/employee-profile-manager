package com.example.employeeprofile.view.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Everything reads MaterialTheme.colorScheme, which swaps light/dark on its own.
private val DarkColors = darkColorScheme(
    primary = DarkAccent,
    onPrimary = DarkOnAccent,
    primaryContainer = DarkAccentMuted,        // selected chip, avatar fill
    onPrimaryContainer = DarkOnAccentMuted,
    secondaryContainer = DarkBadge,            // department badge
    onSecondaryContainer = DarkOnBadge,
    tertiary = DarkActive,                     // active status
    onTertiary = DarkOnActive,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,                     // list cards / form fields
    onSurface = DarkOnSurface,
    surfaceContainerHigh = DarkSurfaceHigh,    // sheets, dialogs, top bar
    onSurfaceVariant = DarkOnSurfaceMuted,     // muted text
    outline = DarkOutline,                     // field borders
    outlineVariant = DarkOutlineVariant,       // dividers
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorMuted
)

private val LightColors = lightColorScheme(
    primary = LightAccent,
    onPrimary = LightOnAccent,
    primaryContainer = LightAccentMuted,
    onPrimaryContainer = LightOnAccentMuted,
    secondaryContainer = LightBadge,
    onSecondaryContainer = LightOnBadge,
    tertiary = LightActive,
    onTertiary = LightOnActive,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceContainerHigh = LightSurfaceHigh,
    onSurfaceVariant = LightOnSurfaceMuted,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorMuted
)

@Composable
fun EmployeeProfileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
