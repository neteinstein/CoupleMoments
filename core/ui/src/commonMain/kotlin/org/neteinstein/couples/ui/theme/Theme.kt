package org.neteinstein.couples.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val lightColorScheme =
    lightColorScheme(
        primary = PrimaryLight,
        onPrimary = OnPrimaryLight,
        primaryContainer = PrimaryContainerLight,
        onPrimaryContainer = OnPrimaryContainerLight,
        secondary = SecondaryLight,
        onSecondary = OnSecondaryLight,
        secondaryContainer = SecondaryContainerLight,
        onSecondaryContainer = OnSecondaryContainerLight,
        tertiary = TertiaryLight,
        onTertiary = OnTertiaryLight,
        tertiaryContainer = TertiaryContainerLight,
        onTertiaryContainer = OnTertiaryContainerLight,
        error = ErrorLight,
        onError = OnErrorLight,
        errorContainer = ErrorContainerLight,
        onErrorContainer = OnErrorContainerLight,
        background = BackgroundLight,
        onBackground = OnBackgroundLight,
        surface = SurfaceLight,
        onSurface = OnSurfaceLight,
        surfaceVariant = SurfaceVariantLight,
        onSurfaceVariant = OnSurfaceVariantLight,
        outline = OutlineLight,
    )

private val darkColorScheme =
    darkColorScheme(
        primary = PrimaryDark,
        onPrimary = OnPrimaryDark,
        primaryContainer = PrimaryContainerDark,
        onPrimaryContainer = OnPrimaryContainerDark,
        secondary = SecondaryDark,
        onSecondary = OnSecondaryDark,
        secondaryContainer = SecondaryContainerDark,
        onSecondaryContainer = OnSecondaryContainerDark,
        tertiary = TertiaryDark,
        onTertiary = OnTertiaryDark,
        tertiaryContainer = TertiaryContainerDark,
        onTertiaryContainer = OnTertiaryContainerDark,
        error = ErrorDark,
        onError = OnErrorDark,
        errorContainer = ErrorContainerDark,
        onErrorContainer = OnErrorContainerDark,
        background = BackgroundDark,
        onBackground = OnBackgroundDark,
        surface = SurfaceDark,
        onSurface = OnSurfaceDark,
        surfaceVariant = SurfaceVariantDark,
        onSurfaceVariant = OnSurfaceVariantDark,
        outline = OutlineDark,
    )

/**
 * Platform hook for Material You dynamic color (Android 12+/API 31+, via
 * `dynamicLightColorScheme`/`dynamicDarkColorScheme` reading the device wallpaper through the
 * platform's own context/window APIs). Returns `null` on platforms/OS versions with no dynamic
 * color support, in which case [CoupleMomentsTheme] falls back to the static [lightColorScheme]/
 * [darkColorScheme] defined above.
 */
@Composable
expect fun platformColorScheme(darkTheme: Boolean): ColorScheme?

/**
 * Platform hook for theme-driven OS chrome that lives outside Compose's own draw tree (Android:
 * status bar icon appearance, via `WindowCompat`'s insets controller). No-op on platforms with no
 * equivalent surface.
 */
@Composable
expect fun PlatformStatusBarEffect(darkTheme: Boolean)

@Composable
fun CoupleMomentsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        (if (dynamicColor) platformColorScheme(darkTheme) else null)
            ?: if (darkTheme) darkColorScheme else lightColorScheme

    PlatformStatusBarEffect(darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CoupleMomentsTypography,
        content = content,
    )
}
