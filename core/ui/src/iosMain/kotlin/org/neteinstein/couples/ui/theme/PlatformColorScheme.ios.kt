package org.neteinstein.couples.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * iOS has no wallpaper-derived Material You palette, so there is never a platform scheme to
 * return - `null` makes [CoupleMomentsTheme] fall back to the app's own static light/dark
 * schemes, which is the intended look on this platform.
 */
@Composable
actual fun platformColorScheme(darkTheme: Boolean): ColorScheme? = null

/**
 * The iOS status bar's appearance follows the hosting UIViewController, which Compose
 * Multiplatform already drives from the app's own theme - nothing to do here.
 */
@Composable
actual fun PlatformStatusBarEffect(darkTheme: Boolean) {
}
