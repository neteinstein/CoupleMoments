package org.neteinstein.couples.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.neteinstein.couples.feature.home.HomeScreen
import org.neteinstein.couples.feature.settings.SettingsScreen
import org.neteinstein.couples.feature.splash.SplashScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = {
                    navController.navigateUp()
                },
            )
        }
    }
}
