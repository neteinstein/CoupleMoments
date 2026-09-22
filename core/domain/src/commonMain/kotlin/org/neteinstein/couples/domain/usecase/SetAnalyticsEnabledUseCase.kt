package org.neteinstein.couples.domain.usecase

import org.neteinstein.couples.domain.analytics.AnalyticsTracker
import org.neteinstein.couples.domain.analytics.AnalyticsUserProperty
import org.neteinstein.couples.domain.repository.AnalyticsConsentRepository
import org.neteinstein.couples.domain.repository.AnalyticsUserIdRepository

/**
 * Persists the Settings > Privacy choice and pushes it straight into the SDK, so opting out takes
 * effect immediately rather than on next launch.
 *
 * Opting out also clears the user id: leaving it set would let events the SDK had already queued
 * but not yet uploaded still arrive attributed to this install. Opting back in re-attaches the
 * same stored id, so a user who toggles it off and on again stays one user rather than two.
 */
class SetAnalyticsEnabledUseCase(
    private val analyticsConsentRepository: AnalyticsConsentRepository,
    private val analyticsUserIdRepository: AnalyticsUserIdRepository,
    private val analyticsTracker: AnalyticsTracker,
) {
    suspend operator fun invoke(enabled: Boolean) {
        analyticsConsentRepository.setEnabled(enabled)
        analyticsTracker.setCollectionEnabled(enabled)
        if (enabled) {
            analyticsTracker.setUserId(analyticsUserIdRepository.getOrCreate())
            analyticsTracker.setUserProperty(AnalyticsUserProperty.Platform, analyticsTracker.platform)
        } else {
            analyticsTracker.setUserId(null)
        }
    }
}
