package org.neteinstein.couples.data.di

import android.content.Context
import androidx.room.Room
import com.russhwolf.settings.SharedPreferencesSettings
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.neteinstein.couples.data.installer.AppUpdateInstallerImpl
import org.neteinstein.couples.data.local.CoupleMomentsDatabase
import org.neteinstein.couples.data.locale.LocaleProviderImpl
import org.neteinstein.couples.data.repository.GitHubUpdateRepositoryImpl
import org.neteinstein.couples.data.repository.IntimacyGateRepositoryImpl
import org.neteinstein.couples.data.repository.QuestionRepositoryImpl
import org.neteinstein.couples.data.repository.QuestionsForParentsRepositoryImpl
import org.neteinstein.couples.data.repository.ThemeModeRepositoryImpl
import org.neteinstein.couples.data.repository.UsedQuestionsRepositoryImpl
import org.neteinstein.couples.domain.repository.AppUpdateInstaller
import org.neteinstein.couples.domain.repository.IntimacyGateRepository
import org.neteinstein.couples.domain.repository.LocaleProvider
import org.neteinstein.couples.domain.repository.QuestionRepository
import org.neteinstein.couples.domain.repository.QuestionsForParentsRepository
import org.neteinstein.couples.domain.repository.ThemeModeRepository
import org.neteinstein.couples.domain.repository.UpdateRepository
import org.neteinstein.couples.domain.repository.UsedQuestionsRepository
import org.neteinstein.couples.domain.usecase.AcknowledgeIntimacyGateUseCase
import org.neteinstein.couples.domain.usecase.CheckForUpdateUseCase
import org.neteinstein.couples.domain.usecase.ClearDownloadedUpdateUseCase
import org.neteinstein.couples.domain.usecase.DownloadAppUpdateUseCase
import org.neteinstein.couples.domain.usecase.GetQuestionsUseCase
import org.neteinstein.couples.domain.usecase.GetRandomQuestionUseCase
import org.neteinstein.couples.domain.usecase.GetThemeModeUseCase
import org.neteinstein.couples.domain.usecase.GetUsedQuestionIdsUseCase
import org.neteinstein.couples.domain.usecase.HasAcknowledgedIntimacyGateUseCase
import org.neteinstein.couples.domain.usecase.IsQuestionsForParentsEnabledUseCase
import org.neteinstein.couples.domain.usecase.MarkQuestionUsedUseCase
import org.neteinstein.couples.domain.usecase.ResetUsedQuestionsUseCase
import org.neteinstein.couples.domain.usecase.SetQuestionsForParentsEnabledUseCase
import org.neteinstein.couples.domain.usecase.SetThemeModeUseCase

val dataModule =
    module {
        single {
            Room
                .databaseBuilder(androidContext(), CoupleMomentsDatabase::class.java, "couple_moments.db")
                .addMigrations(CoupleMomentsDatabase.MIGRATION_1_2, CoupleMomentsDatabase.MIGRATION_2_3)
                .build()
        }
        single { get<CoupleMomentsDatabase>().cardDao() }
        single { get<CoupleMomentsDatabase>().seedMetadataDao() }
        single<QuestionRepository> { QuestionRepositoryImpl(get(), get()) }
        single<LocaleProvider> { LocaleProviderImpl() }
        single<UsedQuestionsRepository> { UsedQuestionsRepositoryImpl(get()) }

        // KMP migration groundwork (step 4, sub-step 1 of 3): each repo below now depends on the
        // multiplatform `Settings` interface rather than a raw `Context`. `SharedPreferencesSettings`
        // is the Android actual - it's just a thin wrapper around the exact same
        // `SharedPreferences` file name/keys these repos always used, so existing installs keep
        // their saved values. Each gets its own named `Settings` singleton (bound to its own
        // preexisting prefs file) rather than one shared instance, to preserve on-disk
        // compatibility - these were three separate files before and must stay that way.
        single(named("themeModeSettings")) {
            SharedPreferencesSettings(androidContext().getSharedPreferences("theme_mode", Context.MODE_PRIVATE))
        }
        single(named("intimacyGateSettings")) {
            SharedPreferencesSettings(androidContext().getSharedPreferences("intimacy_gate", Context.MODE_PRIVATE))
        }
        single(named("questionsForParentsSettings")) {
            SharedPreferencesSettings(
                androidContext().getSharedPreferences("questions_for_parents", Context.MODE_PRIVATE),
            )
        }
        single<IntimacyGateRepository> { IntimacyGateRepositoryImpl(get(named("intimacyGateSettings"))) }
        single<ThemeModeRepository> { ThemeModeRepositoryImpl(get(named("themeModeSettings"))) }
        single<QuestionsForParentsRepository> {
            QuestionsForParentsRepositoryImpl(get(named("questionsForParentsSettings")))
        }
        factory { GetRandomQuestionUseCase(get()) }
        factory { GetQuestionsUseCase(get()) }
        factory { GetUsedQuestionIdsUseCase(get()) }
        factory { MarkQuestionUsedUseCase(get()) }
        factory { ResetUsedQuestionsUseCase(get()) }
        factory { HasAcknowledgedIntimacyGateUseCase(get()) }
        factory { AcknowledgeIntimacyGateUseCase(get()) }
        factory { GetThemeModeUseCase(get()) }
        factory { SetThemeModeUseCase(get()) }
        factory { IsQuestionsForParentsEnabledUseCase(get()) }
        factory { SetQuestionsForParentsEnabledUseCase(get()) }

        // KMP migration groundwork (step 4, sub-step 2 of 3): shared Ktor client for
        // GitHubUpdateRepositoryImpl (see its doc comment) - ignoreUnknownKeys is required since
        // GitHub's real release response has many more fields than GitHubReleaseResponse declares.
        single {
            HttpClient(OkHttp) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
        }
        single<UpdateRepository> { GitHubUpdateRepositoryImpl(context = androidContext(), httpClient = get()) }
        single<AppUpdateInstaller> { AppUpdateInstallerImpl(context = androidContext()) }
        factory { CheckForUpdateUseCase(get()) }
        factory { DownloadAppUpdateUseCase(get()) }
        factory { ClearDownloadedUpdateUseCase(get()) }
    }
