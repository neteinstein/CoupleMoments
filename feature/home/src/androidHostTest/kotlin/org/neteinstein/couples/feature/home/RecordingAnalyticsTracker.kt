package org.neteinstein.couples.feature.home

import org.neteinstein.couples.domain.analytics.AnalyticsEvent
import org.neteinstein.couples.domain.analytics.AnalyticsScreen
import org.neteinstein.couples.domain.analytics.AnalyticsTracker
import org.neteinstein.couples.domain.analytics.AnalyticsUserProperty

/**
 * Hand-written [AnalyticsTracker] double that keeps everything it was given.
 *
 * A fake rather than a MockK mock because the assertions here are about *how many* events of a
 * kind were reported and in what order - `HomeViewModel` deliberately suppresses a repeat report
 * of the card already showing - and counting matched invocations through `verify` means writing a
 * matcher with a side effect, which MockK gives no ordering or arity guarantees about.
 */
class RecordingAnalyticsTracker : AnalyticsTracker {
    override val platform: String = "test"

    val events = mutableListOf<AnalyticsEvent>()
    val screens = mutableListOf<AnalyticsScreen>()
    val userProperties = mutableMapOf<AnalyticsUserProperty, String?>()
    var userId: String? = null
        private set
    var collectionEnabled: Boolean? = null
        private set

    override fun setCollectionEnabled(enabled: Boolean) {
        collectionEnabled = enabled
    }

    override fun setUserId(userId: String?) {
        this.userId = userId
    }

    override fun setUserProperty(
        property: AnalyticsUserProperty,
        value: String?,
    ) {
        userProperties[property] = value
    }

    override fun logScreenView(screen: AnalyticsScreen) {
        screens += screen
    }

    override fun logEvent(event: AnalyticsEvent) {
        events += event
    }

    fun eventsNamed(name: String): List<AnalyticsEvent> = events.filter { it.name == name }
}
