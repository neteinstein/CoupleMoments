package org.neteinstein.couples.feature.settings.platform

import androidx.compose.runtime.Composable
import platform.Foundation.NSBundle

@Composable
actual fun rememberOpenLanguageSettingsAction(): (() -> Unit)? = null

@Composable
actual fun rememberCurrentVersionName(): String {
    val versionKey = "CFBundleShortVersionString"
    return (NSBundle.mainBundle.infoDictionary?.get(versionKey) as? String) ?: "—"
}
