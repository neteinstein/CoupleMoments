package org.neteinstein.couples.data.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import org.neteinstein.couples.domain.analytics.AnalyticsEvent
import org.neteinstein.couples.domain.analytics.AnalyticsScreen
import org.neteinstein.couples.domain.analytics.AnalyticsTracker
import org.neteinstein.couples.domain.analytics.AnalyticsUserProperty

/**
 * Android [AnalyticsTracker], backed by Firebase Analytics.
 *
 * Constructed only when Firebase actually initialized - i.e. when `androidApp/google-services.json`
 * was present at build time, which is what makes the `com.google.gms.google-services` plugin emit
 * the string resources `FirebaseInitProvider` reads at startup. `PlatformDataModule.android.kt`
 * checks for that and binds [NoOpAnalyticsTracker] otherwise, so this class never has to handle an
 * uninitialized SDK.
 *
 * Screen views go through [FirebaseAnalytics.Event.SCREEN_VIEW] rather than a custom event so they
 * land in the console's built-in "Screens and pages" / engagement reports. The SDK's own automatic
 * screen tracking (`ACTIVITY_SCREEN_VIEW`) is useless here - this app has a single Activity and
 * does all its navigation inside Compose - so these calls are the only source of screen data.
 */
class FirebaseAnalyticsTracker(
    private val firebaseAnalytics: FirebaseAnalytics,
) : AnalyticsTracker {
    override val platform: String = PLATFORM_ANDROID

    override fun setCollectionEnabled(enabled: Boolean) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
    }

    override fun setUserId(userId: String?) {
        firebaseAnalytics.setUserId(userId)
    }

    override fun setUserProperty(
        property: AnalyticsUserProperty,
        value: String?,
    ) {
        firebaseAnalytics.setUserProperty(property.key, value)
    }

    override fun logScreenView(screen: AnalyticsScreen) {
        firebaseAnalytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screen.screenName)
                // Conventionally the Activity/Fragment class name. There is only one Activity, so
                // repeating the screen name keeps the console's "screen class" breakdown as
                // readable as its screen-name one instead of collapsing every row into MainActivity.
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screen.screenName)
            },
        )
    }

    override fun logEvent(event: AnalyticsEvent) {
        firebaseAnalytics.logEvent(
            event.name,
            Bundle().apply {
                event.params.forEach { (key, value) -> putString(key, value) }
            },
        )
    }

    companion object {
        const val PLATFORM_ANDROID = "android"
    }
}
