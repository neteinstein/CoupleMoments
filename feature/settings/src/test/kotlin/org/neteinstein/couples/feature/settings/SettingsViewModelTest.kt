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
import org.neteinstein.couples.domain.model.AppUpdate
import org.neteinstein.couples.domain.model.ThemeMode
import org.neteinstein.couples.domain.model.UpdateCheckResult
import org.neteinstein.couples.domain.repository.AppUpdateInstaller
import org.neteinstein.couples.domain.usecase.CheckForUpdateUseCase
import org.neteinstein.couples.domain.usecase.DownloadAppUpdateUseCase
import org.neteinstein.couples.domain.usecase.GetThemeModeUseCase
import org.neteinstein.couples.domain.usecase.IsQuestionsForParentsEnabledUseCase
import org.neteinstein.couples.domain.usecase.ResetUsedQuestionsUseCase
import org.neteinstein.couples.domain.usecase.SetQuestionsForParentsEnabledUseCase
import org.neteinstein.couples.domain.usecase.SetThemeModeUseCase
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val checkForUpdateUseCase: CheckForUpdateUseCase = mockk()
    private val downloadAppUpdateUseCase: DownloadAppUpdateUseCase = mockk()
    private val appUpdateInstaller: AppUpdateInstaller = mockk(relaxUnitFun = true)
    private val resetUsedQuestionsUseCase: ResetUsedQuestionsUseCase = mockk()
    private val themeModeState = MutableStateFlow(ThemeMode.System)
    private val getThemeModeUseCase: GetThemeModeUseCase = mockk()
    private val setThemeModeUseCase: SetThemeModeUseCase = mockk(relaxUnitFun = true)
    private val isQuestionsForParentsEnabledUseCase: IsQuestionsForParentsEnabledUseCase = mockk()
    private val setQuestionsForParentsEnabledUseCase: SetQuestionsForParentsEnabledUseCase = mockk()

    private val update = AppUpdate(versionName = "1.0.6", apkDownloadUrl = "https://example.com/app.apk")

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getThemeModeUseCase() } returns themeModeState
        coEvery { isQuestionsForParentsEnabledUseCase() } returns false
        coEvery { setQuestionsForParentsEnabledUseCase(any()) } returns Unit
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

            val apkFile = File("/tmp/app.apk")
            every { appUpdateInstaller.canInstallPackages() } returns true
            coEvery { downloadAppUpdateUseCase(update) } returns Result.success(apkFile)

            viewModel.onUpdateClicked()
            testDispatcher.scheduler.advanceUntilIdle()

            verify { appUpdateInstaller.installPackage(apkFile) }
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
            coEvery { downloadAppUpdateUseCase(update) } returns Result.success(File("/tmp/app.apk"))

            viewModel.onUpdateClicked()
            testDispatcher.scheduler.advanceUntilIdle()

            verify { appUpdateInstaller.installPackage(any()) }
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
}
