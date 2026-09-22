package org.neteinstein.couples.data.di

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module
import org.neteinstein.couples.data.analytics.NoOpAnalyticsTracker
import org.neteinstein.couples.data.installer.NoOpAppUpdateInstaller
import org.neteinstein.couples.data.local.CardDao
import org.neteinstein.couples.data.local.CardDaoImpl
import org.neteinstein.couples.data.local.CoupleMomentsDatabase
import org.neteinstein.couples.data.local.DriverFactory
import org.neteinstein.couples.data.local.SeedMetadataDao
import org.neteinstein.couples.data.local.SeedMetadataDaoImpl
import org.neteinstein.couples.data.locale.LocaleProviderImpl
import org.neteinstein.couples.data.repository.NoOpUpdateRepository
import org.neteinstein.couples.domain.analytics.AnalyticsTracker
import org.neteinstein.couples.domain.repository.AppUpdateInstaller
import org.neteinstein.couples.domain.repository.LocaleProvider
import org.neteinstein.couples.domain.repository.UpdateRepository
import platform.Foundation.NSUserDefaults

/**
 * iOS gets the same real SQLDelight persistence Android has, through the native driver - the
 * generated database, both DAO implementations and every repository above it are common code, so
 * only the driver differs.
 *
 * Self-update is bound to no-ops: Apple does not allow an app to install another build of itself,
 * so `feature:settings` never shows the "Updates" section here (the `updatesEnabled` flag is
 * always false off Android) and these exist purely so Koin can satisfy the injection.
 */
actual val platformDataModule: Module =
    module {
        single { CoupleMomentsDatabase(DriverFactory().createDriver()) }
        single<CardDao> { CardDaoImpl(get<CoupleMomentsDatabase>().cardQueries) }
        single<SeedMetadataDao> { SeedMetadataDaoImpl(get<CoupleMomentsDatabase>().seedMetadataQueries) }
        single<LocaleProvider> { LocaleProviderImpl() }

        // One NSUserDefaults suite per preference, mirroring Android's one-file-per-preference
        // split (see SettingsQualifiers.kt).
        single<Settings>(themeModeSettings) { NSUserDefaultsSettings(NSUserDefaults(suiteName = "theme_mode")) }
        single<Settings>(intimacyGateSettings) { NSUserDefaultsSettings(NSUserDefaults(suiteName = "intimacy_gate")) }
        single<Settings>(questionsForParentsSettings) {
            NSUserDefaultsSettings(NSUserDefaults(suiteName = "questions_for_parents"))
        }
        single<Settings>(languageSettings) { NSUserDefaultsSettings(NSUserDefaults(suiteName = "app_language")) }
        single<Settings>(analyticsSettings) { NSUserDefaultsSettings(NSUserDefaults(suiteName = "analytics")) }

        // No Firebase on iOS yet: its SDK is distributed through CocoaPods/SPM and would have to
        // be added to the iosApp Xcode project plus a GoogleService-Info.plist, none of which the
        // Gradle build can do. The interface is still bound so commonMain call sites compile and
        // run unchanged here - see NoOpAnalyticsTracker.
        single<AnalyticsTracker> { NoOpAnalyticsTracker(PLATFORM_IOS) }

        single<UpdateRepository> { NoOpUpdateRepository() }
        single<AppUpdateInstaller> { NoOpAppUpdateInstaller() }
    }

private const val PLATFORM_IOS = "ios"
