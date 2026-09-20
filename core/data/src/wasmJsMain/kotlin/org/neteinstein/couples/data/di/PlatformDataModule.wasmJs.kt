package org.neteinstein.couples.data.di

import com.russhwolf.settings.StorageSettings
import org.koin.core.module.Module
import org.koin.dsl.module
import org.neteinstein.couples.data.installer.NoOpAppUpdateInstaller
import org.neteinstein.couples.data.local.CardDao
import org.neteinstein.couples.data.local.SeedMetadataDao
import org.neteinstein.couples.data.local.WebCardDao
import org.neteinstein.couples.data.local.WebSeedMetadataDao
import org.neteinstein.couples.data.locale.LocaleProviderImpl
import org.neteinstein.couples.data.repository.NoOpUpdateRepository
import org.neteinstein.couples.domain.repository.AppUpdateInstaller
import org.neteinstein.couples.domain.repository.LocaleProvider
import org.neteinstein.couples.domain.repository.UpdateRepository

/**
 * The web build has no SQLDelight driver (see [WebCardDao]) and no self-update (a web page is
 * always "latest" - the deploy replaces it), so both of those are bound to storage-backed and
 * no-op implementations respectively.
 *
 * `StorageSettings` is multiplatform-settings' `localStorage` implementation. Unlike Android and
 * iOS there is only one underlying store available, so the four qualifiers all resolve to it -
 * they stay distinct singletons purely so the common wiring reads the same on every platform. The
 * keys the four repositories use don't collide, so sharing the store is safe.
 */
actual val platformDataModule: Module =
    module {
        single<CardDao> { WebCardDao(get(themeModeSettings)) }
        single<SeedMetadataDao> { WebSeedMetadataDao() }
        single<LocaleProvider> { LocaleProviderImpl() }

        single(themeModeSettings) { StorageSettings() }
        single(intimacyGateSettings) { StorageSettings() }
        single(questionsForParentsSettings) { StorageSettings() }
        single(languageSettings) { StorageSettings() }

        single<UpdateRepository> { NoOpUpdateRepository() }
        single<AppUpdateInstaller> { NoOpAppUpdateInstaller() }
    }
