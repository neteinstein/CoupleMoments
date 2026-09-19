package org.neteinstein.couples.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Android's Material You dynamic color scheme, read from the device wallpaper via
 * [LocalContext]. Only available on API 31+ (Android 12); returns `null` below that, exactly as
 * [org.neteinstein.couples.ui.theme.CoupleMomentsTheme] expects so it falls back to the static
 * scheme. Moved verbatim from the pre-KMP `Theme.kt`.
 */
@Composable
actual fun platformColorScheme(darkTheme: Boolean): ColorScheme? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    val context = LocalContext.current
    return if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
}

/**
 * Sets the status bar icon appearance (light/dark) to match [darkTheme] via
 * [WindowCompat]'s insets controller. Moved verbatim from the pre-KMP `Theme.kt`, where it ran as
 * a `SideEffect` directly inside `CoupleMomentsTheme`.
 */
@Composable
actual fun PlatformStatusBarEffect(darkTheme: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
}
