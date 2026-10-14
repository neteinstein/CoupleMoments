package org.neteinstein.couples.data.di

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import org.koin.core.module.Module
import org.koin.dsl.module
import org.neteinstein.couples.data.installer.NoOpAppUpdateInstaller
import org.neteinstein.couples.data.local.CardDao
import org.neteinstein.couples.data.local.IndexedDbCardDao
import org.neteinstein.couples.data.local.IndexedDbDatabase
import org.neteinstein.couples.data.local.IndexedDbSeedMetadataDao
import org.neteinstein.couples.data.local.SeedMetadataDao
import org.neteinstein.couples.data.local.WebCardDao
import org.neteinstein.couples.data.local.WebSeedMetadataDao
import org.neteinstein.couples.data.locale.LocaleProviderImpl
import org.neteinstein.couples.data.repository.NoOpUpdateRepository
import org.neteinstein.couples.domain.repository.AppUpdateInstaller
import org.neteinstein.couples.domain.repository.LocaleProvider
import org.neteinstein.couples.domain.repository.UpdateRepository

/**
 * The web build has no SQLDelight driver, so cards and seed metadata live in IndexedDB instead
 * (see [IndexedDbDatabase]), falling back to [WebCardDao]/[WebSeedMetadataDao] when the browser
 * won't open it. There is no self-update either (a web page is always "latest" - the deploy
 * replaces it), so that is bound to no-op implementations.
 *
 * `StorageSettings` is multiplatform-settings' `localStorage` implementation. Unlike Android and
 * iOS there is only one underlying store available, so the four qualifiers all resolve to it -
 * they stay distinct singletons purely so the common wiring reads the same on every platform. The
 * keys the four repositories use don't collide, so sharing the store is safe.
 */
actual val platformDataModule: Module =
    module {
        single { IndexedDbDatabase() }
        single<CardDao> {
            IndexedDbCardDao(
                database = get(),
                legacySettings = get(themeModeSettings),
                fallback = WebCardDao(get(themeModeSettings)),
            )
        }
        single<SeedMetadataDao> { IndexedDbSeedMetadataDao(database = get(), fallback = WebSeedMetadataDao()) }
        single<LocaleProvider> { LocaleProviderImpl() }

        single<Settings>(themeModeSettings) { StorageSettings() }
        single<Settings>(intimacyGateSettings) { StorageSettings() }
        single<Settings>(questionsForParentsSettings) { StorageSettings() }
        single<Settings>(languageSettings) { StorageSettings() }

        single<UpdateRepository> { NoOpUpdateRepository() }
        single<AppUpdateInstaller> { NoOpAppUpdateInstaller() }
    }
