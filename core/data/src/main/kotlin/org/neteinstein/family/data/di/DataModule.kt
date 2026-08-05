package org.neteinstein.family.data.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.neteinstein.family.data.installer.AppUpdateInstallerImpl
import org.neteinstein.family.data.locale.LocaleProviderImpl
import org.neteinstein.family.data.repository.GitHubUpdateRepositoryImpl
import org.neteinstein.family.data.repository.QuestionRepositoryImpl
import org.neteinstein.family.data.repository.UsedQuestionsRepositoryImpl
import org.neteinstein.family.domain.repository.AppUpdateInstaller
import org.neteinstein.family.domain.repository.LocaleProvider
import org.neteinstein.family.domain.repository.QuestionRepository
import org.neteinstein.family.domain.repository.UpdateRepository
import org.neteinstein.family.domain.repository.UsedQuestionsRepository
import org.neteinstein.family.domain.usecase.CheckForUpdateUseCase
import org.neteinstein.family.domain.usecase.ClearDownloadedUpdateUseCase
import org.neteinstein.family.domain.usecase.DownloadAppUpdateUseCase
import org.neteinstein.family.domain.usecase.GetQuestionsUseCase
import org.neteinstein.family.domain.usecase.GetRandomQuestionUseCase
import org.neteinstein.family.domain.usecase.GetUsedQuestionIdsUseCase
import org.neteinstein.family.domain.usecase.MarkQuestionUsedUseCase

val dataModule = module {
    single<QuestionRepository> { QuestionRepositoryImpl() }
    single<LocaleProvider> { LocaleProviderImpl() }
    single<UsedQuestionsRepository> { UsedQuestionsRepositoryImpl(context = androidContext()) }
    factory { GetRandomQuestionUseCase(get()) }
    factory { GetQuestionsUseCase(get()) }
    factory { GetUsedQuestionIdsUseCase(get()) }
    factory { MarkQuestionUsedUseCase(get()) }

    single<UpdateRepository> { GitHubUpdateRepositoryImpl(context = androidContext()) }
    single<AppUpdateInstaller> { AppUpdateInstallerImpl(context = androidContext()) }
    factory { CheckForUpdateUseCase(get()) }
    factory { DownloadAppUpdateUseCase(get()) }
    factory { ClearDownloadedUpdateUseCase(get()) }
}
