package org.neteinstein.family.feature.settings

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.File
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
import org.neteinstein.family.domain.model.AppUpdate
import org.neteinstein.family.domain.model.UpdateCheckResult
import org.neteinstein.family.domain.repository.AppUpdateInstaller
import org.neteinstein.family.domain.usecase.CheckForUpdateUseCase
import org.neteinstein.family.domain.usecase.DownloadAppUpdateUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val checkForUpdateUseCase: CheckForUpdateUseCase = mockk()
    private val downloadAppUpdateUseCase: DownloadAppUpdateUseCase = mockk()
    private val appUpdateInstaller: AppUpdateInstaller = mockk(relaxUnitFun = true)

    private val update = AppUpdate(versionName = "1.0.6", apkDownloadUrl = "https://example.com/app.apk")

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SettingsViewModel(checkForUpdateUseCase, downloadAppUpdateUseCase, appUpdateInstaller)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onScreenEntered sets UpToDate when no update is available`() = runTest {
        coEvery { checkForUpdateUseCase() } returns Result.success(UpdateCheckResult.UpToDate("1.0.5"))

        viewModel.onScreenEntered()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(UpdateStatus.UpToDate("1.0.5"), viewModel.uiState.value.updateStatus)
    }

    @Test
    fun `onScreenEntered sets UpdateAvailable when a newer release exists`() = runTest {
        coEvery { checkForUpdateUseCase() } returns Result.success(UpdateCheckResult.UpdateAvailable(update))

        viewModel.onScreenEntered()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(UpdateStatus.UpdateAvailable(update), viewModel.uiState.value.updateStatus)
    }

    @Test
    fun `onUpdateClicked downloads and installs when an update is already known`() = runTest {
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
    fun `onUpdateClicked stops at SideloadingBlocked when the OS blocks installs`() = runTest {
        coEvery { checkForUpdateUseCase() } returns Result.success(UpdateCheckResult.UpdateAvailable(update))
        viewModel.onScreenEntered()
        testDispatcher.scheduler.advanceUntilIdle()

        every { appUpdateInstaller.canInstallPackages() } returns false

        viewModel.onUpdateClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(UpdateStatus.SideloadingBlocked, viewModel.uiState.value.updateStatus)
    }

    @Test
    fun `onUpdateClicked with no known update checks first then downloads`() = runTest {
        coEvery { checkForUpdateUseCase() } returns Result.success(UpdateCheckResult.UpdateAvailable(update))
        every { appUpdateInstaller.canInstallPackages() } returns true
        coEvery { downloadAppUpdateUseCase(update) } returns Result.success(File("/tmp/app.apk"))

        viewModel.onUpdateClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        verify { appUpdateInstaller.installPackage(any()) }
    }

    @Test
    fun `onUpdateClicked surfaces a failure message when the check fails`() = runTest {
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
}
