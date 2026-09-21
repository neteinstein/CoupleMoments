package org.neteinstein.couples

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import org.koin.compose.viewmodel.koinViewModel
import org.neteinstein.couples.domain.model.ThemeMode
import org.neteinstein.couples.navigation.AppNavigation
import org.neteinstein.couples.ui.PlatformInstallAppBanner
import org.neteinstein.couples.ui.applyAppLanguageOverride
import org.neteinstein.couples.ui.theme.CoupleMomentsTheme

/**
 * Shared entry point composable, called from `MainActivity` (androidApp), `MainViewController`
 * (iosApp, via `app`'s iosMain) and `main()` (webApp). Koin must already be started (see
 * [org.neteinstein.couples.di.doInitKoin] and androidApp's `CoupleMomentsApp`) before this
 * composes, since [koinViewModel] resolves [MainViewModel] from it.
 */
@Composable
fun App() {
    val viewModel: MainViewModel = koinViewModel()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val languageOverride by viewModel.languageOverride.collectAsStateWithLifecycle()
    // Applied here, above the UI it affects, so the very first composition of the tree below
    // already resolves its strings in the chosen language - see applyAppLanguageOverride.
    remember(languageOverride) { applyAppLanguageOverride(languageOverride?.code) }
    val darkTheme =
        when (themeMode) {
            ThemeMode.Light -> false
            ThemeMode.Dark -> true
            ThemeMode.System -> isSystemInDarkTheme()
        }

    CoupleMomentsTheme(darkTheme = darkTheme, dynamicColor = false) {
        // Compose Resources caches the strings it has already resolved against the locale that
        // was active when they were read, and nothing about a locale change invalidates that. So
        // the whole UI is keyed on the override and rebuilt when it changes - the NavController
        // is created outside the key so the user stays on the screen they changed it from.
        val navController = rememberNavController()
        key(languageOverride) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Renders nothing outside the web build - see PlatformInstallAppBanner's kdoc.
                PlatformInstallAppBanner()
                AppNavigation(navController = navController)
            }
        }
    }
}
