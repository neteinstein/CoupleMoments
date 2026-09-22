package org.neteinstein.couples.data.analytics

import org.neteinstein.couples.domain.analytics.AnalyticsEvent
import org.neteinstein.couples.domain.analytics.AnalyticsScreen
import org.neteinstein.couples.domain.analytics.AnalyticsTracker
import org.neteinstein.couples.domain.analytics.AnalyticsUserProperty

/**
 * [AnalyticsTracker] that discards everything. Bound wherever no analytics SDK is present:
 *
 * - **iOS**, which has no Firebase SDK in this build at all (adding it means a CocoaPods/SPM
 *   dependency in `iosApp`, not a Gradle one - see AGENTS.md).
 * - **Android without a `google-services.json`**, i.e. every local build by a contributor who
 *   doesn't have the Firebase config - see `PlatformDataModule.android.kt`.
 *
 * Existing so the rest of the app never branches on "is analytics configured": call sites always
 * get a tracker, and the decision is made once, in DI.
 */
class NoOpAnalyticsTracker(
    override val platform: String,
) : AnalyticsTracker {
    override fun setCollectionEnabled(enabled: Boolean) = Unit

    override fun setUserId(userId: String?) = Unit

    override fun setUserProperty(
        property: AnalyticsUserProperty,
        value: String?,
    ) = Unit

    override fun logScreenView(screen: AnalyticsScreen) = Unit

    override fun logEvent(event: AnalyticsEvent) = Unit
}
