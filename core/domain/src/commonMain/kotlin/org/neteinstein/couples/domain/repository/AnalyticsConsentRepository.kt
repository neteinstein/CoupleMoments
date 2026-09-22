package org.neteinstein.couples.domain.repository

/**
 * Whether the user lets the app report anonymous usage analytics - the Settings > Privacy toggle.
 *
 * Defaults to enabled, and is applied to the SDK itself (not just to this app's call sites) via
 * [org.neteinstein.couples.domain.analytics.AnalyticsTracker.setCollectionEnabled], so opting out
 * stops Firebase's own automatic events (`first_open`, `session_start`, `screen_view`) too rather
 * than only the ones this app logs explicitly.
 */
interface AnalyticsConsentRepository {
    suspend fun isEnabled(): Boolean

    suspend fun setEnabled(enabled: Boolean)
}
