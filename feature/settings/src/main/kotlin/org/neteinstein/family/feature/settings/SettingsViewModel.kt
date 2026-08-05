package org.neteinstein.family.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.neteinstein.family.domain.model.AppUpdate
import org.neteinstein.family.domain.model.UpdateCheckResult
import org.neteinstein.family.domain.repository.AppUpdateInstaller
import org.neteinstein.family.domain.usecase.CheckForUpdateUseCase
import org.neteinstein.family.domain.usecase.DownloadAppUpdateUseCase

/**
 * Runs a background update check when Settings is entered so the "Update to latest" button can
 * reflect availability immediately (green when a release is found), without auto-downloading.
 */
class SettingsViewModel(
    private val checkForUpdateUseCase: CheckForUpdateUseCase,
    private val downloadAppUpdateUseCase: DownloadAppUpdateUseCase,
    private val appUpdateInstaller: AppUpdateInstaller
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onScreenEntered() {
        viewModelScope.launch {
            when (val result = checkForUpdateUseCase().getOrNull()) {
                is UpdateCheckResult.UpToDate ->
                    _uiState.update { it.copy(updateStatus = UpdateStatus.UpToDate(result.currentVersionName)) }
                is UpdateCheckResult.UpdateAvailable ->
                    _uiState.update { it.copy(updateStatus = UpdateStatus.UpdateAvailable(result.update)) }
                null -> Unit
            }
        }
    }

    /**
     * Checks GitHub Releases and, if a newer build exists, downloads it and launches the system
     * installer - unless [AppUpdateInstaller.canInstallPackages] says the OS will block that
     * install outright, in which case this stops at [UpdateStatus.SideloadingBlocked] without
     * downloading anything.
     */
    fun onUpdateClicked() {
        val availableUpdate = (_uiState.value.updateStatus as? UpdateStatus.UpdateAvailable)?.update
        if (availableUpdate != null) {
            viewModelScope.launch { downloadAndInstall(availableUpdate) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(updateStatus = UpdateStatus.Checking) }
            checkForUpdateUseCase()
                .onSuccess { result -> handleCheckResultForUpdateClick(result) }
                .onFailure { error ->
                    _uiState.update { it.copy(updateStatus = UpdateStatus.Failed(error.toUserMessage())) }
                }
        }
    }

    /** Deep-links to the system "install unknown apps" settings page for this app. */
    fun onEnableSideloadingClicked() {
        appUpdateInstaller.openInstallPermissionSettings()
    }

    private suspend fun handleCheckResultForUpdateClick(result: UpdateCheckResult) {
        when (result) {
            is UpdateCheckResult.UpToDate ->
                _uiState.update { it.copy(updateStatus = UpdateStatus.UpToDate(result.currentVersionName)) }
            is UpdateCheckResult.UpdateAvailable -> downloadAndInstall(result.update)
        }
    }

    private suspend fun downloadAndInstall(update: AppUpdate) {
        if (!appUpdateInstaller.canInstallPackages()) {
            _uiState.update { it.copy(updateStatus = UpdateStatus.SideloadingBlocked) }
            return
        }

        _uiState.update { it.copy(updateStatus = UpdateStatus.Downloading) }
        downloadAppUpdateUseCase(update)
            .onSuccess { apkFile ->
                appUpdateInstaller.installPackage(apkFile)
                _uiState.update { it.copy(updateStatus = UpdateStatus.Idle) }
            }.onFailure { error ->
                _uiState.update { it.copy(updateStatus = UpdateStatus.Failed(error.toUserMessage())) }
            }
    }

    private fun Throwable.toUserMessage(): String = message ?: "Something went wrong"
}
