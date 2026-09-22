package org.neteinstein.couples.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.neteinstein.couples.analytics.TrackScreenView
import org.neteinstein.couples.domain.analytics.AnalyticsScreen
import org.neteinstein.couples.feature.settings.SettingsScreen
import org.neteinstein.couples.feature.splash.SplashScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
    ) {
        composable(Screen.Splash.route) {
            TrackScreenView(AnalyticsScreen.Splash)
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }
        // Screen.Home has no TrackScreenView of its own: MainScreen reports whichever of its two
        // tabs is showing, which is what a user would call the screen they are on.
        composable(Screen.Home.route) {
            MainScreen(
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
            )
        }
        composable(Screen.Settings.route) {
            TrackScreenView(AnalyticsScreen.Settings)
            SettingsScreen(
                onBack = {
                    navController.navigateUp()
                },
            )
        }
    }
}
