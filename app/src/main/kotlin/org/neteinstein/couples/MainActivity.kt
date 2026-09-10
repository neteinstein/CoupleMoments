package org.neteinstein.couples

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import org.neteinstein.couples.navigation.AppNavigation
import org.neteinstein.couples.ui.theme.CoupleMomentsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CoupleMomentsTheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
}
