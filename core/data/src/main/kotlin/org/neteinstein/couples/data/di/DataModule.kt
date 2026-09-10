package org.neteinstein.couples.data.di

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.neteinstein.couples.data.installer.AppUpdateInstallerImpl
import org.neteinstein.couples.data.local.CoupleMomentsDatabase
import org.neteinstein.couples.data.locale.LocaleProviderImpl
import org.neteinstein.couples.data.repository.GitHubUpdateRepositoryImpl
import org.neteinstein.couples.data.repository.IntimacyGateRepositoryImpl
import org.neteinstein.couples.data.repository.QuestionRepositoryImpl
import org.neteinstein.couples.data.repository.UsedQuestionsRepositoryImpl
import org.neteinstein.couples.domain.repository.AppUpdateInstaller
import org.neteinstein.couples.domain.repository.IntimacyGateRepository
import org.neteinstein.couples.domain.repository.LocaleProvider
import org.neteinstein.couples.domain.repository.QuestionRepository
import org.neteinstein.couples.domain.repository.UpdateRepository
import org.neteinstein.couples.domain.repository.UsedQuestionsRepository
import org.neteinstein.couples.domain.usecase.AcknowledgeIntimacyGateUseCase
import org.neteinstein.couples.domain.usecase.CheckForUpdateUseCase
import org.neteinstein.couples.domain.usecase.ClearDownloadedUpdateUseCase
import org.neteinstein.couples.domain.usecase.DownloadAppUpdateUseCase
import org.neteinstein.couples.domain.usecase.GetQuestionsUseCase
import org.neteinstein.couples.domain.usecase.GetRandomQuestionUseCase
import org.neteinstein.couples.domain.usecase.GetUsedQuestionIdsUseCase
import org.neteinstein.couples.domain.usecase.HasAcknowledgedIntimacyGateUseCase
import org.neteinstein.couples.domain.usecase.MarkQuestionUsedUseCase
import org.neteinstein.couples.domain.usecase.ResetUsedQuestionsUseCase

val dataModule =
    module {
        single {
            Room
                .databaseBuilder(androidContext(), CoupleMomentsDatabase::class.java, "couple_moments.db")
                .addMigrations(CoupleMomentsDatabase.MIGRATION_1_2)
                .build()
        }
        single { get<CoupleMomentsDatabase>().cardDao() }
        single { get<CoupleMomentsDatabase>().seedMetadataDao() }
        single<QuestionRepository> { QuestionRepositoryImpl(get(), get()) }
        single<LocaleProvider> { LocaleProviderImpl() }
        single<UsedQuestionsRepository> { UsedQuestionsRepositoryImpl(get()) }
        single<IntimacyGateRepository> { IntimacyGateRepositoryImpl(androidContext()) }
        factory { GetRandomQuestionUseCase(get()) }
        factory { GetQuestionsUseCase(get()) }
        factory { GetUsedQuestionIdsUseCase(get()) }
        factory { MarkQuestionUsedUseCase(get()) }
        factory { ResetUsedQuestionsUseCase(get()) }
        factory { HasAcknowledgedIntimacyGateUseCase(get()) }
        factory { AcknowledgeIntimacyGateUseCase(get()) }

        single<UpdateRepository> { GitHubUpdateRepositoryImpl(context = androidContext()) }
        single<AppUpdateInstaller> { AppUpdateInstallerImpl(context = androidContext()) }
        factory { CheckForUpdateUseCase(get()) }
        factory { DownloadAppUpdateUseCase(get()) }
        factory { ClearDownloadedUpdateUseCase(get()) }
    }
