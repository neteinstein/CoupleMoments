package org.neteinstein.couples.feature.settings.platform

import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberOpenLanguageSettingsAction(): (() -> Unit)? {
    val context = LocalContext.current
    return remember(context) { { context.startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS)) } }
}

// getPackageInfo(String, Int) is deprecated in favor of the PackageInfoFlags overload added in
// API 33, but minSdk is 32 - there's no non-deprecated way to read this below API 33.
@Suppress("DEPRECATION")
@Composable
actual fun rememberCurrentVersionName(): String {
    val context = LocalContext.current
    return remember(context) {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "—"
    }
}
