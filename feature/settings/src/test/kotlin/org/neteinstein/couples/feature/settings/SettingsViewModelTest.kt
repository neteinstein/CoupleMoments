package org.neteinstein.couples.feature.settings

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.neteinstein.couples.domain.model.UpdateCheckResult
import org.neteinstein.couples.domain.repository.AppUpdateInstaller
import org.neteinstein.couples.domain.usecase.CheckForUpdateUseCase
import org.neteinstein.couples.domain.usecase.DownloadAppUpdateUseCase
import org.neteinstein.couples.domain.usecase.ResetUsedQuestionsUseCase
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val checkForUpdateUseCase: CheckForUpdateUseCase = mockk()
    private val downloadAppUpdateUseCase: DownloadAppUpdateUseCase = mockk()
    private val appUpdateInstaller: AppUpdateInstaller = mockk(relaxUnitFun = true)
    private val resetUsedQuestionsUseCase: ResetUsedQuestionsUseCase = mockk()

    private val update = AppUpdate(versionName = "1.0.6", apkDownloadUrl = "https://example.com/app.apk")

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel =
            SettingsViewModel(
                checkForUpdateUseCase,
                downloadAppUpdateUseCase,
                appUpdateInstaller,
                resetUsedQuestionsUseCase,
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
    fun `onScreenEntered skips the update check when updates are disabled`() =
        runTest {
            val playStoreViewModel =
                SettingsViewModel(
                    checkForUpdateUseCase,
                    downloadAppUpdateUseCase,
                    appUpdateInstaller,
                    resetUsedQuestionsUseCase,
                    updatesEnabled = false,
                )

            playStoreViewModel.onScreenEntered()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 0) { checkForUpdateUseCase() }
            assertEquals(false, playStoreViewModel.uiState.value.updatesEnabled)
            assertEquals(UpdateStatus.Idle, playStoreViewModel.uiState.value.updateStatus)
        }
}
