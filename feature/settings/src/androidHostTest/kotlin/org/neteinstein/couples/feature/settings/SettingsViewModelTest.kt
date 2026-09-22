package org.neteinstein.couples.feature.settings

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.neteinstein.couples.domain.analytics.AnalyticsTracker
import org.neteinstein.couples.domain.analytics.AnalyticsUserProperty
import org.neteinstein.couples.domain.model.AppLanguage
import org.neteinstein.couples.domain.model.AppUpdate
import org.neteinstein.couples.domain.model.Question
import org.neteinstein.couples.domain.model.QuestionAudience
import org.neteinstein.couples.domain.model.ThemeMode
import org.neteinstein.couples.domain.model.UpdateCheckResult
import org.neteinstein.couples.domain.repository.AppUpdateInstaller
import org.neteinstein.couples.domain.usecase.CheckForUpdateUseCase
import org.neteinstein.couples.domain.usecase.DownloadAppUpdateUseCase
import org.neteinstein.couples.domain.usecase.GetContentLanguageUseCase
import org.neteinstein.couples.domain.usecase.GetLanguageOverrideUseCase
import org.neteinstein.couples.domain.usecase.GetQuestionsUseCase
import org.neteinstein.couples.domain.usecase.GetThemeModeUseCase
import org.neteinstein.couples.domain.usecase.GetUsedQuestionIdsUseCase
import org.neteinstein.couples.domain.usecase.IsAnalyticsEnabledUseCase
import org.neteinstein.couples.domain.usecase.IsQuestionsForParentsEnabledUseCase
import org.neteinstein.couples.domain.usecase.ResetUsedQuestionsUseCase
import org.neteinstein.couples.domain.usecase.SetAnalyticsEnabledUseCase
import org.neteinstein.couples.domain.usecase.SetLanguageOverrideUseCase
import org.neteinstein.couples.domain.usecase.SetQuestionsForParentsEnabledUseCase
import org.neteinstein.couples.domain.usecase.SetThemeModeUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val checkForUpdateUseCase: CheckForUpdateUseCase = mockk()
    private val downloadAppUpdateUseCase: DownloadAppUpdateUseCase = mockk()
    private val appUpdateInstaller: AppUpdateInstaller = mockk(relaxUnitFun = true)
    private val isAnalyticsEnabledUseCase: IsAnalyticsEnabledUseCase = mockk()
    private val setAnalyticsEnabledUseCase: SetAnalyticsEnabledUseCase = mockk(relaxed = true)
    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)
    private val resetUsedQuestionsUseCase: ResetUsedQuestionsUseCase = mockk()
    private val themeModeState = MutableStateFlow(ThemeMode.System)
    private val getThemeModeUseCase: GetThemeModeUseCase = mockk()
    private val setThemeModeUseCase: SetThemeModeUseCase = mockk(relaxUnitFun = true)
    private val isQuestionsForParentsEnabledUseCase: IsQuestionsForParentsEnabledUseCase = mockk()
    private val setQuestionsForParentsEnabledUseCase: SetQuestionsForParentsEnabledUseCase = mockk()
    private val getQuestionsUseCase: GetQuestionsUseCase = mockk()
    private val getUsedQuestionIdsUseCase: GetUsedQuestionIdsUseCase = mockk()
    private val getContentLanguageUseCase: GetContentLanguageUseCase = mockk()
    private val languageOverrideState = MutableStateFlow<AppLanguage?>(null)
    private val getLanguageOverrideUseCase: GetLanguageOverrideUseCase = mockk()
    private val setLanguageOverrideUseCase: SetLanguageOverrideUseCase = mockk(relaxUnitFun = true)

    private val update = AppUpdate(versionName = "1.0.6", apkDownloadUrl = "https://example.com/app.apk")

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getThemeModeUseCase() } returns themeModeState
        coEvery { isQuestionsForParentsEnabledUseCase() } returns false
        coEvery { setQuestionsForParentsEnabledUseCase(any()) } returns Unit
        every { getContentLanguageUseCase() } returns "en"
        every { getLanguageOverrideUseCase() } returns languageOverrideState
        coEvery { getQuestionsUseCase("en") } returns emptyList()
        coEvery { getUsedQuestionIdsUseCase() } returns emptySet()
        coEvery { isAnalyticsEnabledUseCase() } returns true
        viewModel =
            SettingsViewModel(
                checkForUpdateUseCase,
                downloadAppUpdateUseCase,
                appUpdateInstaller,
                resetUsedQuestionsUseCase,
                getThemeModeUseCase,
                setThemeModeUseCase,
                isQuestionsForParentsEnabledUseCase,
                setQuestionsForParentsEnabledUseCase,
                getQuestionsUseCase,
                getUsedQuestionIdsUseCase,
                getContentLanguageUseCase,
                getLanguageOverrideUseCase,
                setLanguageOverrideUseCase,
                isAnalyticsEnabledUseCase,
                setAnalyticsEnabledUseCase,
                analyticsTracker,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onScreenEntered sets UpToDate when no update is available`() =
        runTest {
            coEvery { checkForUpdateUseCase() } returns Result.success(UpdateCheckResult.UpToDate("1.0.5"))

            viewModel.onScreenEntered()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(UpdateStatus.UpToDate("1.0.5"), viewModel.uiState.value.updateStatus)
        }

    @Test
    fun `onScreenEntered sets UpdateAvailable when a newer release exists`() =
        runTest {
            coEvery { checkForUpdateUseCase() } returns Result.success(UpdateCheckResult.UpdateAvailable(update))

            viewModel.onScreenEntered()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(UpdateStatus.UpdateAvailable(update), viewModel.uiState.value.updateStatus)
        }

    @Test
    fun `onUpdateClicked downloads and installs when an update is already known`() =
        runTest {
            coEvery { checkForUpdateUseCase() } returns Result.success(UpdateCheckResult.UpdateAvailable(update))
            viewModel.onScreenEntered()
            testDispatcher.scheduler.advanceUntilIdle()

            val apkBytes = byteArrayOf(1, 2, 3)
            every { appUpdateInstaller.canInstallPackages() } returns true
            coEvery { downloadAppUpdateUseCase(update) } returns Result.success(apkBytes)

            viewModel.onUpdateClicked()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { appUpdateInstaller.installPackage(any()) }
            assertEquals(UpdateStatus.Idle, viewModel.uiState.value.updateStatus)
        }

    @Test
    fun `onUpdateClicked stops at SideloadingBlocked when the OS blocks installs`() =
        runTest {
            coEvery { checkForUpdateUseCase() } returns Result.success(UpdateCheckResult.UpdateAvailable(update))
            viewModel.onScreenEntered()
            testDispatcher.scheduler.advanceUntilIdle()

            every { appUpdateInstaller.canInstallPackages() } returns false

            viewModel.onUpdateClicked()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(UpdateStatus.SideloadingBlocked, viewModel.uiState.value.updateStatus)
        }

    @Test
    fun `onUpdateClicked with no known update checks first then downloads`() =
        runTest {
            coEvery { checkForUpdateUseCase() } returns Result.success(UpdateCheckResult.UpdateAvailable(update))
            every { appUpdateInstaller.canInstallPackages() } returns true
            coEvery { downloadAppUpdateUseCase(update) } returns Result.success(byteArrayOf(1, 2, 3))

            viewModel.onUpdateClicked()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { appUpdateInstaller.installPackage(any()) }
        }

    @Test
    fun `onUpdateClicked surfaces a failure message when the check fails`() =
        runTest {
            coEvery { checkForUpdateUseCase() } returns Result.failure(IllegalStateException("network error"))

            viewModel.onUpdateClicked()
            testDispatcher.scheduler.advanceUntilIdle()

            val status = viewModel.uiState.value.updateStatus
            assertTrue(status is UpdateStatus.Failed)
            assertEquals("network error", (status as UpdateStatus.Failed).message)
        }

    @Test
    fun `onEnableSideloadingClicked opens the install permission settings`() {
        viewModel.onEnableSideloadingClicked()

        verify { appUpdateInstaller.openInstallPermissionSettings() }
    }

    @Test
    fun `onResetCardsClicked resets used questions and reports done`() =
        runTest {
            coEvery { resetUsedQuestionsUseCase() } returns Unit

            viewModel.onResetCardsClicked()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { resetUsedQuestionsUseCase() }
            assertEquals(ResetCardsStatus.Done, viewModel.uiState.value.resetCardsStatus)
        }

    @Test
    fun `updatesEnabled defaults to true so the Updates section shows by default`() {
        assertTrue(viewModel.uiState.value.updatesEnabled)
    }

    @Test
    fun `uiState reflects the persisted theme mode`() {
        assertEquals(ThemeMode.System, viewModel.uiState.value.themeMode)
    }

    @Test
    fun `onThemeModeSelected persists the chosen mode and updates uiState`() =
        runTest {
            coEvery { setThemeModeUseCase(ThemeMode.Dark) } answers { themeModeState.value = ThemeMode.Dark }

            viewModel.onThemeModeSelected(ThemeMode.Dark)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { setThemeModeUseCase(ThemeMode.Dark) }
            assertEquals(ThemeMode.Dark, viewModel.uiState.value.themeMode)
        }

    @Test
    fun `onScreenEntered skips the update check when updates are disabled`() =
        runTest {
            val playStoreViewModel =
                SettingsViewModel(
                    checkForUpdateUseCase,
                    downloadAppUpdateUseCase,
                    appUpdateInstaller,
                    resetUsedQuestionsUseCase,
                    getThemeModeUseCase,
                    setThemeModeUseCase,
                    isQuestionsForParentsEnabledUseCase,
                    setQuestionsForParentsEnabledUseCase,
                    getQuestionsUseCase,
                    getUsedQuestionIdsUseCase,
                    getContentLanguageUseCase,
                    getLanguageOverrideUseCase,
                    setLanguageOverrideUseCase,
                    isAnalyticsEnabledUseCase,
                    setAnalyticsEnabledUseCase,
                    analyticsTracker,
                    updatesEnabled = false,
                )

            playStoreViewModel.onScreenEntered()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 0) { checkForUpdateUseCase() }
            assertEquals(false, playStoreViewModel.uiState.value.updatesEnabled)
            assertEquals(UpdateStatus.Idle, playStoreViewModel.uiState.value.updateStatus)
        }

    @Test
    fun `questionsForParentsEnabled defaults to false so the toggle starts off`() {
        assertEquals(false, viewModel.uiState.value.questionsForParentsEnabled)
    }

    @Test
    fun `onScreenEntered loads the persisted questionsForParents value`() =
        runTest {
            coEvery { isQuestionsForParentsEnabledUseCase() } returns true
            coEvery { checkForUpdateUseCase() } returns Result.success(UpdateCheckResult.UpToDate("1.0.5"))

            viewModel.onScreenEntered()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(true, viewModel.uiState.value.questionsForParentsEnabled)
        }

    @Test
    fun `onQuestionsForParentsToggled persists the new value and reflects it immediately`() =
        runTest {
            viewModel.onQuestionsForParentsToggled(true)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { setQuestionsForParentsEnabledUseCase(true) }
            assertEquals(true, viewModel.uiState.value.questionsForParentsEnabled)
        }

    @Test
    fun `onScreenEntered counts hidden cards against the audience-filtered total`() =
        runTest {
            val questions =
                listOf(
                    Question(id = 1, text = "a", languageCode = "en"),
                    Question(id = 2, text = "b", languageCode = "en"),
                    Question(id = 3, text = "c", languageCode = "en", audience = QuestionAudience.WithKids),
                )
            coEvery { getQuestionsUseCase("en") } returns questions
            coEvery { getUsedQuestionIdsUseCase() } returns setOf(1)
            coEvery { checkForUpdateUseCase() } returns Result.success(UpdateCheckResult.UpToDate("1.0.5"))

            viewModel.onScreenEntered()
            testDispatcher.scheduler.advanceUntilIdle()

            // questionsForParentsEnabled is false, so the WithKids question is excluded from the total.
            assertEquals(2, viewModel.uiState.value.totalCardsCount)
            assertEquals(1, viewModel.uiState.value.hiddenCardsCount)
        }

    @Test
    fun `onResetCardsClicked refreshes counts to zero hidden after resetting`() =
        runTest {
            coEvery { getQuestionsUseCase("en") } returns
                listOf(Question(id = 1, text = "a", languageCode = "en"))
            // onResetCardsClicked refreshes counts only after resetUsedQuestionsUseCase() runs, so this
            // stands in for the now-cleared hidden set rather than a "before" value.
            coEvery { getUsedQuestionIdsUseCase() } returns emptySet()
            coEvery { resetUsedQuestionsUseCase() } returns Unit

            viewModel.onResetCardsClicked()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(0, viewModel.uiState.value.hiddenCardsCount)
            assertEquals(1, viewModel.uiState.value.totalCardsCount)
        }

    @Test
    fun `onScreenEntered loads the persisted analytics opt-in`() =
        runTest {
            coEvery { isAnalyticsEnabledUseCase() } returns false
            coEvery { checkForUpdateUseCase() } returns Result.success(UpdateCheckResult.UpToDate("1.0.5"))

            viewModel.onScreenEntered()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(false, viewModel.uiState.value.analyticsEnabled)
        }

    /**
     * Opting out is the last thing an install reports, so the event has to be logged before
     * [SetAnalyticsEnabledUseCase] shuts collection off - otherwise the opt-out rate is
     * unobservable, which is the one number this toggle exists to produce.
     */
    @Test
    fun `opting out reports the toggle before collection stops`() =
        runTest {
            viewModel.onAnalyticsEnabledToggled(false)
            testDispatcher.scheduler.advanceUntilIdle()

            verify {
                analyticsTracker.logEvent(
                    match { it.name == "analytics_toggled" && it.params["enabled"] == "false" },
                )
            }
            coVerify { setAnalyticsEnabledUseCase(false) }
            assertEquals(false, viewModel.uiState.value.analyticsEnabled)
        }

    @Test
    fun `changing the theme reports it as both an event and a user property`() =
        runTest {
            viewModel.onThemeModeSelected(ThemeMode.Dark)
            testDispatcher.scheduler.advanceUntilIdle()

            verify {
                analyticsTracker.logEvent(
                    match { it.name == "theme_changed" && it.params["theme"] == "dark" },
                )
            }
            verify { analyticsTracker.setUserProperty(AnalyticsUserProperty.ThemeMode, "dark") }
        }
}
