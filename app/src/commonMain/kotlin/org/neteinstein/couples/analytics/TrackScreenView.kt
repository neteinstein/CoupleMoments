package org.neteinstein.couples.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.koinInject
import org.neteinstein.couples.domain.analytics.AnalyticsScreen
import org.neteinstein.couples.domain.analytics.AnalyticsTracker

/**
 * Reports [screen] as a `screen_view` for as long as it is on screen, re-reporting whenever
 * [screen] changes.
 *
 * Firebase's automatic screen tracking is Activity-based and therefore blind to this app, which
 * has one Activity and does all its navigation inside Compose (and, on iOS/Web, no Activity at
 * all) - so these calls are the only source of screen data in the console's engagement reports.
 *
 * Placed at each destination rather than driven off `NavController.currentBackStackEntryFlow`
 * because the two tabs inside the single `home` destination are separate screens to the user but
 * indistinguishable in the back stack - see [org.neteinstein.couples.navigation.MainScreen].
 * Keying the effect on [screen] means a tab switch reports the new one and a re-entry from
 * Settings reports the tab the user actually returns to.
 */
@Composable
fun TrackScreenView(screen: AnalyticsScreen) {
    val analyticsTracker: AnalyticsTracker = koinInject()
    LaunchedEffect(screen) {
        analyticsTracker.logScreenView(screen)
    }
}
