package org.neteinstein.couples.data.di

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import org.koin.core.module.Module
import org.koin.dsl.module
import org.neteinstein.couples.data.analytics.FirebaseWebAnalyticsTracker
import org.neteinstein.couples.data.installer.NoOpAppUpdateInstaller
import org.neteinstein.couples.data.local.CardDao
import org.neteinstein.couples.data.local.SeedMetadataDao
import org.neteinstein.couples.data.local.WebCardDao
import org.neteinstein.couples.data.local.WebSeedMetadataDao
import org.neteinstein.couples.data.locale.LocaleProviderImpl
import org.neteinstein.couples.data.repository.NoOpUpdateRepository
import org.neteinstein.couples.domain.analytics.AnalyticsTracker
import org.neteinstein.couples.domain.repository.AppUpdateInstaller
import org.neteinstein.couples.domain.repository.LocaleProvider
import org.neteinstein.couples.domain.repository.UpdateRepository

/**
 * The web build has no SQLDelight driver (see [WebCardDao]) and no self-update (a web page is
 * always "latest" - the deploy replaces it), so both of those are bound to storage-backed and
 * no-op implementations respectively.
 *
 * `StorageSettings` is multiplatform-settings' `localStorage` implementation. Unlike Android and
 * iOS there is only one underlying store available, so the five qualifiers all resolve to it -
 * they stay distinct singletons purely so the common wiring reads the same on every platform. The
 * keys the five repositories use don't collide, so sharing the store is safe - which is exactly
 * why `AnalyticsConsentRepositoryImpl`'s key is "analytics_enabled" and not a bare "enabled".
 */
actual val platformDataModule: Module =
    module {
        single<CardDao> { WebCardDao(get(themeModeSettings)) }
        single<SeedMetadataDao> { WebSeedMetadataDao() }
        single<LocaleProvider> { LocaleProviderImpl() }

        single<Settings>(themeModeSettings) { StorageSettings() }
        single<Settings>(intimacyGateSettings) { StorageSettings() }
        single<Settings>(questionsForParentsSettings) { StorageSettings() }
        single<Settings>(languageSettings) { StorageSettings() }
        single<Settings>(analyticsSettings) { StorageSettings() }

        // Unconditional, unlike Android's: the tracker talks to a `globalThis` bridge that simply
        // isn't defined when firebase-init.js was generated without a configuration, so an
        // unconfigured build is already a no-op at the call boundary - see
        // FirebaseWebAnalyticsTracker.
        single<AnalyticsTracker> { FirebaseWebAnalyticsTracker() }

        single<UpdateRepository> { NoOpUpdateRepository() }
        single<AppUpdateInstaller> { NoOpAppUpdateInstaller() }
    }
