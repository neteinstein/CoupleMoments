package org.neteinstein.couples.data.di

import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.neteinstein.couples.domain.analytics.AnalyticsTracker
import org.neteinstein.couples.domain.repository.AnalyticsConsentRepository
import org.neteinstein.couples.domain.repository.AnalyticsUserIdRepository
import org.neteinstein.couples.domain.repository.IntimacyGateRepository
import org.neteinstein.couples.domain.repository.LanguagePreferenceRepository
import org.neteinstein.couples.domain.repository.LocaleProvider
import org.neteinstein.couples.domain.repository.QuestionRepository
import org.neteinstein.couples.domain.repository.QuestionsForParentsRepository
import org.neteinstein.couples.domain.repository.ThemeModeRepository
import org.neteinstein.couples.domain.repository.UsedQuestionsRepository
import org.neteinstein.couples.domain.usecase.GetContentLanguageUseCase
import org.neteinstein.couples.domain.usecase.GetThemeModeUseCase
import org.neteinstein.couples.domain.usecase.InitializeAnalyticsUseCase
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertNotNull

/**
 * Actually *resolves* [dataModule]'s graph instead of only compiling against it.
 *
 * This exists because a real startup crash shipped without any gate catching it: each named
 * `Settings` store was registered under its concrete class (`SharedPreferencesSettings` and
 * friends) while every repository asks Koin for the `Settings` interface, and Koin matches on
 * exact type - so the very first `get()` threw `NoDefinitionFoundException` and the app rendered
 * nothing. Everything compiled, every unit test passed, and all three platforms built, because
 * nothing ever built the graph.
 *
 * Resolving the types below is enough to catch that class of mistake: a missing or wrongly-typed
 * binding anywhere underneath them fails here rather than at app launch.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DataModuleGraphTest {
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `every dataModule binding resolves`() {
        val koin =
            startKoin {
                androidContext(ApplicationProvider.getApplicationContext())
                modules(dataModule)
            }.koin

        // The five Settings-backed repositories - the four the shipped bug broke, plus analytics.
        assertNotNull(koin.get<ThemeModeRepository>())
        assertNotNull(koin.get<IntimacyGateRepository>())
        assertNotNull(koin.get<QuestionsForParentsRepository>())
        assertNotNull(koin.get<LanguagePreferenceRepository>())
        assertNotNull(koin.get<AnalyticsConsentRepository>())
        assertNotNull(koin.get<AnalyticsUserIdRepository>())

        // The rest of the graph, so a regression anywhere in it surfaces here too.
        assertNotNull(koin.get<QuestionRepository>())
        assertNotNull(koin.get<UsedQuestionsRepository>())
        assertNotNull(koin.get<LocaleProvider>())
        assertNotNull(koin.get<GetThemeModeUseCase>())
        assertNotNull(koin.get<GetContentLanguageUseCase>())
        // Analytics resolves through the same graph: the tracker is a platformDataModule binding
        // (NoOpAnalyticsTracker here, since Robolectric has no google-services.json behind it) and
        // InitializeAnalyticsUseCase pulls five other definitions in behind it.
        assertNotNull(koin.get<AnalyticsTracker>())
        assertNotNull(koin.get<InitializeAnalyticsUseCase>())
    }
}
