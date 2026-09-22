package org.neteinstein.couples.domain.analytics

/** Records everything handed to it, so tests can assert on what would have been reported. */
class FakeAnalyticsTracker(
    override val platform: String = "test",
) : AnalyticsTracker {
    val events = mutableListOf<AnalyticsEvent>()
    val screens = mutableListOf<AnalyticsScreen>()
    val userProperties = mutableMapOf<AnalyticsUserProperty, String?>()
    val userIds = mutableListOf<String?>()
    val collectionEnabledCalls = mutableListOf<Boolean>()

    override fun setCollectionEnabled(enabled: Boolean) {
        collectionEnabledCalls += enabled
    }

    override fun setUserId(userId: String?) {
        userIds += userId
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
}
