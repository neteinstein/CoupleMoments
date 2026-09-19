package org.neteinstein.couples.ui.component

import androidx.compose.runtime.Composable

/**
 * Intercepts the system back gesture/button while [enabled], invoking [onBack] instead of the
 * default back navigation. Wraps `androidx.activity.compose.BackHandler` on Android - there is no
 * multiplatform artifact for it in this repo's current Compose Multiplatform version, so this
 * stays expect/actual (Android is the only KMP target today; a future iOS/wasmJs actual would need
 * its own platform-appropriate implementation, e.g. a no-op or predictive-back equivalent).
 */
@Composable
expect fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
)
