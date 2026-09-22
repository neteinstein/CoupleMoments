package org.neteinstein.couples.domain.usecase

import org.neteinstein.couples.domain.repository.AnalyticsConsentRepository

class IsAnalyticsEnabledUseCase(
    private val analyticsConsentRepository: AnalyticsConsentRepository,
) {
    suspend operator fun invoke(): Boolean = analyticsConsentRepository.isEnabled()
}
