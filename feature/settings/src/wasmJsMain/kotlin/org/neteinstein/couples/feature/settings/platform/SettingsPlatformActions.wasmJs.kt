package org.neteinstein.couples.feature.settings.platform

import androidx.compose.runtime.Composable

@Composable
actual fun rememberOpenLanguageSettingsAction(): (() -> Unit)? = null

// The web build isn't versioned the way the Android/iOS packages are - it's whatever is currently
// deployed - so the "About" section shows a placeholder rather than a misleading number.
@Composable
actual fun rememberCurrentVersionName(): String = "—"
