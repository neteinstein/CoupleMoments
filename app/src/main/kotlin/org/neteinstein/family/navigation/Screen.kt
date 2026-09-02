package org.neteinstein.family.navigation

sealed class Screen(
    val route: String,
) {
    data object Splash : Screen("splash")

    data object Home : Screen("home")

    data object Settings : Screen("settings")
}
