package org.neteinstein.couples.domain.usecase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.neteinstein.couples.domain.analytics.AnalyticsUserProperty
import org.neteinstein.couples.domain.analytics.FakeAnalyticsTracker
import org.neteinstein.couples.domain.model.AppLanguage
import org.neteinstein.couples.domain.model.ThemeMode
import org.neteinstein.couples.domain.repository.AnalyticsConsentRepository
import org.neteinstein.couples.domain.repository.AnalyticsUserIdRepository
import org.neteinstein.couples.domain.repository.LanguagePreferenceRepository
import org.neteinstein.couples.domain.repository.LocaleProvider
import org.neteinstein.couples.domain.repository.QuestionsForParentsRepository
import org.neteinstein.couples.domain.repository.ThemeModeRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Consent is the part of the analytics wiring where a mistake is not a missing metric but a
 * privacy failure, so both directions of the toggle and the cold-start path are pinned here.
 */
class AnalyticsConsentUseCasesTest {
    private val tracker = FakeAnalyticsTracker(platform = "test-platform")
    private val consentRepository = FakeAnalyticsConsentRepository()
    private val userIdRepository = FakeAnalyticsUserIdRepository()

    private val initializeAnalytics =
        InitializeAnalyticsUseCase(
            analyticsTracker = tracker,
            analyticsConsentRepository = consentRepository,
            analyticsUserIdRepository = userIdRepository,
            getThemeModeUseCase = GetThemeModeUseCase(FakeThemeModeRepository(ThemeMode.Dark)),
            getContentLanguageUseCase =
                GetContentLanguageUseCase(
                    localeProvider = FixedLocaleProvider("pt"),
                    languagePreferenceRepository = FakeLanguagePreferenceRepository(),
                ),
            isQuestionsForParentsEnabledUseCase =
                IsQuestionsForParentsEnabledUseCase(FakeQuestionsForParentsRepository(enabled = true)),
        )

    private val setAnalyticsEnabled =
        SetAnalyticsEnabledUseCase(
            analyticsConsentRepository = consentRepository,
            analyticsUserIdRepository = userIdRepository,
            analyticsTracker = tracker,
        )

    @Test
    fun `initialize attaches the anonymous id and every user property when opted in`() =
        runTest {
            initializeAnalytics()

            assertEquals(listOf(true), tracker.collectionEnabledCalls)
            assertEquals(listOf<String?>(userIdRepository.issuedId), tracker.userIds)
            assertEquals("test-platform", tracker.userProperties[AnalyticsUserProperty.Platform])
            assertEquals("pt", tracker.userProperties[AnalyticsUserProperty.AppLanguage])
            assertEquals("dark", tracker.userProperties[AnalyticsUserProperty.ThemeMode])
            assertEquals("true", tracker.userProperties[AnalyticsUserProperty.ParentsMode])
        }

    /**
     * An opted-out install must not even hand the SDK an id: the point of the toggle is that this
     * device stops being identifiable in the data at all, not that it reports less.
     */
    @Test
    fun `initialize disables collection and reports nothing else when opted out`() =
        runTest {
            consentRepository.enabled = false

            initializeAnalytics()

            assertEquals(listOf(false), tracker.collectionEnabledCalls)
            assertTrue(tracker.userIds.isEmpty())
            assertTrue(tracker.userProperties.isEmpty())
        }

    @Test
    fun `opting out persists the choice, tells the SDK, and clears the user id`() =
        runTest {
            setAnalyticsEnabled(false)

            assertEquals(false, consentRepository.enabled)
            assertEquals(listOf(false), tracker.collectionEnabledCalls)
            assertEquals(listOf<String?>(null), tracker.userIds)
        }

    /** Opting back in must not mint a second identity for the same install. */
    @Test
    fun `opting back in re-attaches the same stored id`() =
        runTest {
            setAnalyticsEnabled(false)
            setAnalyticsEnabled(true)

            assertEquals(true, consentRepository.enabled)
            assertEquals(listOf(null, userIdRepository.issuedId), tracker.userIds.toList())
            assertEquals(1, userIdRepository.createdCount)
        }

    private class FakeAnalyticsConsentRepository(
        var enabled: Boolean = true,
    ) : AnalyticsConsentRepository {
        override suspend fun isEnabled(): Boolean = enabled

        override suspend fun setEnabled(enabled: Boolean) {
            this.enabled = enabled
        }
    }

    private class FakeAnalyticsUserIdRepository : AnalyticsUserIdRepository {
        val issuedId = "0123456789abcdef0123456789abcdef"
        var createdCount = 0
            private set

        override suspend fun getOrCreate(): String {
            createdCount++
            return issuedId
        }
    }

    private class FixedLocaleProvider(
        private val languageCode: String,
    ) : LocaleProvider {
        override fun currentLanguageCode(): String = languageCode
    }

    private class FakeThemeModeRepository(
        mode: ThemeMode,
    ) : ThemeModeRepository {
        override val themeMode: StateFlow<ThemeMode> = MutableStateFlow(mode)

        override suspend fun setThemeMode(mode: ThemeMode) = Unit
    }

    private class FakeLanguagePreferenceRepository : LanguagePreferenceRepository {
        override val languageOverride: StateFlow<AppLanguage?> = MutableStateFlow(null)

        override suspend fun setLanguageOverride(language: AppLanguage?) = Unit
    }

    private class FakeQuestionsForParentsRepository(
        private val enabled: Boolean,
    ) : QuestionsForParentsRepository {
        override suspend fun isEnabled(): Boolean = enabled

        override suspend fun setEnabled(enabled: Boolean) = Unit
    }
}
