package org.neteinstein.couples.data.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.neteinstein.couples.data.repository.IntimacyGateRepositoryImpl
import org.neteinstein.couples.data.repository.LanguagePreferenceRepositoryImpl
import org.neteinstein.couples.data.repository.QuestionRepositoryImpl
import org.neteinstein.couples.data.repository.QuestionsForParentsRepositoryImpl
import org.neteinstein.couples.data.repository.ThemeModeRepositoryImpl
import org.neteinstein.couples.data.repository.UsedQuestionsRepositoryImpl
import org.neteinstein.couples.domain.repository.IntimacyGateRepository
import org.neteinstein.couples.domain.repository.LanguagePreferenceRepository
import org.neteinstein.couples.domain.repository.QuestionRepository
import org.neteinstein.couples.domain.repository.QuestionsForParentsRepository
import org.neteinstein.couples.domain.repository.ThemeModeRepository
import org.neteinstein.couples.domain.repository.UsedQuestionsRepository
import org.neteinstein.couples.domain.usecase.AcknowledgeIntimacyGateUseCase
import org.neteinstein.couples.domain.usecase.CheckForUpdateUseCase
import org.neteinstein.couples.domain.usecase.ClearDownloadedUpdateUseCase
import org.neteinstein.couples.domain.usecase.DownloadAppUpdateUseCase
import org.neteinstein.couples.domain.usecase.GetContentLanguageUseCase
import org.neteinstein.couples.domain.usecase.GetLanguageOverrideUseCase
import org.neteinstein.couples.domain.usecase.GetQuestionsUseCase
import org.neteinstein.couples.domain.usecase.GetRandomQuestionUseCase
import org.neteinstein.couples.domain.usecase.GetThemeModeUseCase
import org.neteinstein.couples.domain.usecase.GetUsedQuestionIdsUseCase
import org.neteinstein.couples.domain.usecase.HasAcknowledgedIntimacyGateUseCase
import org.neteinstein.couples.domain.usecase.IsQuestionsForParentsEnabledUseCase
import org.neteinstein.couples.domain.usecase.MarkQuestionUsedUseCase
import org.neteinstein.couples.domain.usecase.ResetUsedQuestionsUseCase
import org.neteinstein.couples.domain.usecase.SetLanguageOverrideUseCase
import org.neteinstein.couples.domain.usecase.SetQuestionsForParentsEnabledUseCase
import org.neteinstein.couples.domain.usecase.SetThemeModeUseCase

/**
 * Everything in this module is platform-agnostic: the repositories are plain Kotlin over the
 * `CardDao`/`SeedMetadataDao`/`Settings` interfaces, and the use cases wrapping them are
 * `core:domain` code.
 *
 * What each platform actually binds those interfaces to - the SQLDelight driver (or, on Web, a
 * `Settings`-backed DAO), the `Settings` implementation, the `LocaleProvider`, the Ktor engine,
 * and whether self-update exists at all - lives in [platformDataModule]. See each target's actual
 * for the details and the reasoning.
 */
val dataModule =
    module {
        single<QuestionRepository> { QuestionRepositoryImpl(get(), get()) }
        single<UsedQuestionsRepository> { UsedQuestionsRepositoryImpl(get()) }
        single<IntimacyGateRepository> { IntimacyGateRepositoryImpl(get(intimacyGateSettings)) }
        single<ThemeModeRepository> { ThemeModeRepositoryImpl(get(themeModeSettings)) }
        single<QuestionsForParentsRepository> { QuestionsForParentsRepositoryImpl(get(questionsForParentsSettings)) }
        single<LanguagePreferenceRepository> { LanguagePreferenceRepositoryImpl(get(languageSettings)) }
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
        factory { GetContentLanguageUseCase(get(), get()) }
        factory { GetLanguageOverrideUseCase(get()) }
        factory { SetLanguageOverrideUseCase(get()) }
        factory { CheckForUpdateUseCase(get()) }
        factory { DownloadAppUpdateUseCase(get()) }
        factory { ClearDownloadedUpdateUseCase(get()) }

        includes(platformDataModule)
    }

/**
 * Platform bindings for everything [dataModule] resolves but cannot construct itself: `CardDao`/
 * `SeedMetadataDao`, the four named `Settings` stores, `LocaleProvider`, `UpdateRepository` and
 * `AppUpdateInstaller`.
 */
expect val platformDataModule: Module
