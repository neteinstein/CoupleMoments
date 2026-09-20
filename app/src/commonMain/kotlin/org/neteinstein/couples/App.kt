package org.neteinstein.couples

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import org.koin.compose.viewmodel.koinViewModel
import org.neteinstein.couples.domain.model.ThemeMode
import org.neteinstein.couples.navigation.AppNavigation
import org.neteinstein.couples.ui.PlatformInstallAppBanner
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
    val darkTheme =
        when (themeMode) {
            ThemeMode.Light -> false
            ThemeMode.Dark -> true
            ThemeMode.System -> isSystemInDarkTheme()
        }

    CoupleMomentsTheme(darkTheme = darkTheme, dynamicColor = false) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Renders nothing outside the web build - see PlatformInstallAppBanner's kdoc.
            PlatformInstallAppBanner()
            val navController = rememberNavController()
            AppNavigation(navController = navController)
        }
    }
}
