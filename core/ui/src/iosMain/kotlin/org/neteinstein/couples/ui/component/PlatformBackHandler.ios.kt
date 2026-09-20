package org.neteinstein.couples.ui.component

import androidx.compose.runtime.Composable

/**
 * No-op on iOS. iOS has a system swipe-back gesture, but it belongs to the hosting UINavigationController, not to the Compose hierarchy - there is nothing here for Compose to intercept, so this is a no-op and the overlay is dismissed by its own on-screen close control.
 */
@Composable
actual fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
}
