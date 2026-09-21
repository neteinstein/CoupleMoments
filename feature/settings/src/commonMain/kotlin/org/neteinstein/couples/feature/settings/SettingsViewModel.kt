package org.neteinstein.couples.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.neteinstein.couples.domain.model.AppLanguage
import org.neteinstein.couples.domain.model.AppUpdate
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
import org.neteinstein.couples.domain.usecase.IsQuestionsForParentsEnabledUseCase
import org.neteinstein.couples.domain.usecase.ResetUsedQuestionsUseCase
import org.neteinstein.couples.domain.usecase.SetLanguageOverrideUseCase
import org.neteinstein.couples.domain.usecase.SetQuestionsForParentsEnabledUseCase
import org.neteinstein.couples.domain.usecase.SetThemeModeUseCase

/**
 * Runs a background update check when Settings is entered so the "Update to latest" button can
 * reflect availability immediately (green when a release is found), without auto-downloading.
 *
 * [updatesEnabled] is false on the Play Store flavor, which the Play Store itself updates - the
 * self-update check never runs and SettingsScreen hides the "Updates" section entirely.
 */
class SettingsViewModel(
    private val checkForUpdateUseCase: CheckForUpdateUseCase,
    private val downloadAppUpdateUseCase: DownloadAppUpdateUseCase,
    private val appUpdateInstaller: AppUpdateInstaller,
    private val resetUsedQuestionsUseCase: ResetUsedQuestionsUseCase,
    private val getThemeModeUseCase: GetThemeModeUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    private val isQuestionsForParentsEnabledUseCase: IsQuestionsForParentsEnabledUseCase,
    private val setQuestionsForParentsEnabledUseCase: SetQuestionsForParentsEnabledUseCase,
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val getUsedQuestionIdsUseCase: GetUsedQuestionIdsUseCase,
    private val getContentLanguageUseCase: GetContentLanguageUseCase,
    private val getLanguageOverrideUseCase: GetLanguageOverrideUseCase,
    private val setLanguageOverrideUseCase: SetLanguageOverrideUseCase,
    private val updatesEnabled: Boolean = true,
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(
            SettingsUiState(
                updatesEnabled = updatesEnabled,
                themeMode = getThemeModeUseCase().value,
                languageOverride = getLanguageOverrideUseCase().value,
            ),
        )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getThemeModeUseCase().collect { mode -> _uiState.update { it.copy(themeMode = mode) } }
        }
        viewModelScope.launch {
            getLanguageOverrideUseCase().collect { language -> _uiState.update { it.copy(languageOverride = language) } }
        }
    }

    /**
     * Persists the chosen in-app language override (`null` = follow the OS/browser language) and
     * refreshes the card counts, which are language-specific. The picker's own selection comes
     * back through [getLanguageOverrideUseCase]'s flow, collected in [init], which is also what
     * re-localizes the rest of the app. Only ever called from iOS/Web - on
     * Android the language row deep-links into the OS's own per-app language settings instead (see
     * `rememberOpenLanguageSettingsAction`).
     */
    fun onLanguageSelected(language: AppLanguage?) {
        viewModelScope.launch {
            setLanguageOverrideUseCase(language)
            refreshCardCounts(_uiState.value.questionsForParentsEnabled)
        }
    }

    /** Persists the chosen [mode]; [getThemeModeUseCase]'s shared flow reflects it back into [uiState]. */
    fun onThemeModeSelected(mode: ThemeMode) {
        viewModelScope.launch { setThemeModeUseCase(mode) }
    }

    fun onScreenEntered() {
        viewModelScope.launch {
            val enabled = isQuestionsForParentsEnabledUseCase()
            _uiState.update { it.copy(questionsForParentsEnabled = enabled) }
            refreshCardCounts(enabled)
        }
        if (!updatesEnabled) return
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

    /** Persists the "Couple Questions For Parents" toggle and reflects it immediately in the UI. */
    fun onQuestionsForParentsToggled(enabled: Boolean) {
        viewModelScope.launch {
            setQuestionsForParentsEnabledUseCase(enabled)
            _uiState.update { it.copy(questionsForParentsEnabled = enabled) }
            refreshCardCounts(enabled)
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

    /** Makes every card hidden via swipe-down on Home visible again. */
    fun onResetCardsClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(resetCardsStatus = ResetCardsStatus.Resetting) }
            resetUsedQuestionsUseCase()
            refreshCardCounts(_uiState.value.questionsForParentsEnabled)
            _uiState.update { it.copy(resetCardsStatus = ResetCardsStatus.Done) }
        }
    }

    /**
     * Recomputes [SettingsUiState.totalCardsCount]/[SettingsUiState.hiddenCardsCount] for the
     * "Reset Cards" section, mirroring the same audience filter Home applies so the counts match
     * what's actually browsable there.
     */
    private suspend fun refreshCardCounts(questionsForParentsEnabled: Boolean) {
        val languageCode = getContentLanguageUseCase()
        val visibleQuestions =
            getQuestionsUseCase(languageCode)
                .filter { questionsForParentsEnabled || it.audience != QuestionAudience.WithKids }
        val usedQuestionIds = getUsedQuestionIdsUseCase()
        val hiddenCount = visibleQuestions.count { it.id in usedQuestionIds }
        _uiState.update { it.copy(totalCardsCount = visibleQuestions.size, hiddenCardsCount = hiddenCount) }
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
            .onSuccess { apkBytes ->
                // installPackage takes raw bytes and owns where it stages them (a
                // FileProvider-scoped cache directory on Android) - the ViewModel needs no
                // filesystem/Context knowledge of its own.
                appUpdateInstaller.installPackage(apkBytes)
                _uiState.update { it.copy(updateStatus = UpdateStatus.Idle) }
            }.onFailure { error ->
                _uiState.update { it.copy(updateStatus = UpdateStatus.Failed(error.toUserMessage())) }
            }
    }

    private fun Throwable.toUserMessage(): String = message ?: "Something went wrong"
}
