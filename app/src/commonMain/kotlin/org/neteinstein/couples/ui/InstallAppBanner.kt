package org.neteinstein.couples.ui

import androidx.compose.runtime.Composable

/**
 * A dismissible banner above the app content pointing web visitors at the native Android app.
 * Web-only: on Android the app *is* the native app, and on iOS there's nothing to install yet, so
 * both of those actuals render nothing at all.
 */
@Composable
expect fun PlatformInstallAppBanner()
