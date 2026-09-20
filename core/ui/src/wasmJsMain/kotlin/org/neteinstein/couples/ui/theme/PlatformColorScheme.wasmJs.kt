package org.neteinstein.couples.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * A browser has no Material You palette to read, so there is never a platform scheme to return -
 * `null` makes [CoupleMomentsTheme] fall back to the app's own static light/dark schemes.
 */
@Composable
actual fun platformColorScheme(darkTheme: Boolean): ColorScheme? = null

/** A browser page has no status bar. */
@Composable
actual fun PlatformStatusBarEffect(darkTheme: Boolean) {
}
