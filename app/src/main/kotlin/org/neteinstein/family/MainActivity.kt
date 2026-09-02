package org.neteinstein.family

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import org.neteinstein.family.navigation.AppNavigation
import org.neteinstein.family.ui.theme.FamilyMomentsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FamilyMomentsTheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
}
