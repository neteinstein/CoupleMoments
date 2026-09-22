package org.neteinstein.couples.domain.usecase

import org.neteinstein.couples.domain.analytics.AnalyticsTracker
import org.neteinstein.couples.domain.analytics.AnalyticsUserProperty
import org.neteinstein.couples.domain.analytics.analyticsValue
import org.neteinstein.couples.domain.repository.AnalyticsConsentRepository
import org.neteinstein.couples.domain.repository.AnalyticsUserIdRepository

/**
 * Brings the analytics SDK up to date with this install before anything is reported: applies the
 * persisted opt-in/out, attaches the anonymous user id, and seeds the user properties every later
 * event is segmented by.
 *
 * Called once per process from `MainViewModel`'s init - the root ViewModel, which exists on all
 * three platforms and is created before any screen composes. Consent is applied *first* so an
 * opted-out install never hands the SDK an id or a property at all.
 *
 * The properties are also refreshed at their point of change (see `SettingsViewModel`); this is
 * the cold-start path that covers a user who changed one, reinstalled, or is on a device where
 * the previously-set value has expired.
 */
class InitializeAnalyticsUseCase(
    private val analyticsTracker: AnalyticsTracker,
    private val analyticsConsentRepository: AnalyticsConsentRepository,
    private val analyticsUserIdRepository: AnalyticsUserIdRepository,
    private val getThemeModeUseCase: GetThemeModeUseCase,
    private val getContentLanguageUseCase: GetContentLanguageUseCase,
    private val isQuestionsForParentsEnabledUseCase: IsQuestionsForParentsEnabledUseCase,
) {
    suspend operator fun invoke() {
        val enabled = analyticsConsentRepository.isEnabled()
        analyticsTracker.setCollectionEnabled(enabled)
        if (!enabled) return

        analyticsTracker.setUserId(analyticsUserIdRepository.getOrCreate())
        analyticsTracker.setUserProperty(AnalyticsUserProperty.Platform, analyticsTracker.platform)
        analyticsTracker.setUserProperty(AnalyticsUserProperty.AppLanguage, getContentLanguageUseCase())
        analyticsTracker.setUserProperty(AnalyticsUserProperty.ThemeMode, getThemeModeUseCase().value.analyticsValue())
        analyticsTracker.setUserProperty(
            AnalyticsUserProperty.ParentsMode,
            isQuestionsForParentsEnabledUseCase().toString(),
        )
    }
}
