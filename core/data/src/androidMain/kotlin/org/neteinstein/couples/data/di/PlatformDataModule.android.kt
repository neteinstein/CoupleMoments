package org.neteinstein.couples.data.di

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import org.neteinstein.couples.data.installer.AppUpdateInstallerImpl
import org.neteinstein.couples.data.local.CardDao
import org.neteinstein.couples.data.local.CardDaoImpl
import org.neteinstein.couples.data.local.CoupleMomentsDatabase
import org.neteinstein.couples.data.local.DriverFactory
import org.neteinstein.couples.data.local.SeedMetadataDao
import org.neteinstein.couples.data.local.SeedMetadataDaoImpl
import org.neteinstein.couples.data.locale.LocaleProviderImpl
import org.neteinstein.couples.data.repository.GitHubUpdateRepositoryImpl
import org.neteinstein.couples.domain.repository.AppUpdateInstaller
import org.neteinstein.couples.domain.repository.LocaleProvider
import org.neteinstein.couples.domain.repository.UpdateRepository

/**
 * Android is the only target with the full feature set: real SQLDelight persistence, and the
 * GitHub-Releases self-update flow (APK sideloading has no iOS/Web equivalent).
 *
 * Each `Settings` store is bound to the exact `SharedPreferences` file name its repository used
 * before the KMP migration, so upgrading installs keep their saved theme, intimacy-gate
 * acknowledgement and questions-for-parents choice.
 */
actual val platformDataModule: Module =
    module {
        single { CoupleMomentsDatabase(DriverFactory(androidContext()).createDriver()) }
        single<CardDao> { CardDaoImpl(get<CoupleMomentsDatabase>().cardQueries) }
        single<SeedMetadataDao> { SeedMetadataDaoImpl(get<CoupleMomentsDatabase>().seedMetadataQueries) }
        single<LocaleProvider> { LocaleProviderImpl() }

        single<Settings>(themeModeSettings) {
            SharedPreferencesSettings(androidContext().getSharedPreferences("theme_mode", Context.MODE_PRIVATE))
        }
        single<Settings>(intimacyGateSettings) {
            SharedPreferencesSettings(androidContext().getSharedPreferences("intimacy_gate", Context.MODE_PRIVATE))
        }
        single<Settings>(questionsForParentsSettings) {
            SharedPreferencesSettings(
                androidContext().getSharedPreferences("questions_for_parents", Context.MODE_PRIVATE),
            )
        }
        // New in the multiplatform build - Android has no pre-existing file to stay compatible
        // with here, since the language was always an OS-level per-app setting on this platform.
        single<Settings>(languageSettings) {
            SharedPreferencesSettings(androidContext().getSharedPreferences("app_language", Context.MODE_PRIVATE))
        }

        // ignoreUnknownKeys is required since GitHub's real release response has many more fields
        // than GitHubReleaseResponse declares.
        single {
            HttpClient(OkHttp) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
        }
        single<UpdateRepository> { GitHubUpdateRepositoryImpl(context = androidContext(), httpClient = get()) }
        single<AppUpdateInstaller> { AppUpdateInstallerImpl(context = androidContext()) }
    }
