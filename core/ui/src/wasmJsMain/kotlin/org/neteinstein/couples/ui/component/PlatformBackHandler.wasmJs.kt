package org.neteinstein.couples.ui.component

import androidx.compose.runtime.Composable

/**
 * No-op on the web. The browser's back button navigates the page history, not the app's internal overlay state; hijacking it would break the back button's meaning for the whole site, so this is a no-op and the overlay is dismissed by its own on-screen close control.
 */
@Composable
actual fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
}
