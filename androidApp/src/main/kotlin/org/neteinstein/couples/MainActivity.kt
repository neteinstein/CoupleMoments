package org.neteinstein.couples

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

/**
 * Thin Android shell around `app`'s shared [App] composable - the theme resolution, navigation and
 * ViewModel wiring that used to live here are all commonMain code now. Only the genuinely
 * Android-specific pieces stay: the system splash screen and edge-to-edge window setup.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}
