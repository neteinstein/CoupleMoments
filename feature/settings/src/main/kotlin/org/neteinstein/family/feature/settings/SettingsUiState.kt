package org.neteinstein.family.feature.settings

import org.neteinstein.family.domain.model.AppUpdate

data class SettingsUiState(
    val updateStatus: UpdateStatus = UpdateStatus.Idle,
    val resetCardsStatus: ResetCardsStatus = ResetCardsStatus.Idle,
    /** False on the Play Store flavor, which hides the "Updates" section entirely - see SettingsViewModel. */
    val updatesEnabled: Boolean = true,
)

/** Drives the "Reset Cards" button and its status text on the Settings screen. */
sealed class ResetCardsStatus {
    data object Idle : ResetCardsStatus()

    data object Resetting : ResetCardsStatus()

    data object Done : ResetCardsStatus()
}

/** Drives the "Update to latest" button and its status text on the Settings screen. */
sealed class UpdateStatus {
    /** Nothing in flight - the button's normal resting state. */
    data object Idle : UpdateStatus()

    data object Checking : UpdateStatus()

    /** The installed build is already the latest one published on GitHub Releases. */
    data class UpToDate(
        val currentVersionName: String,
    ) : UpdateStatus()

    /** A newer build exists and is ready to be downloaded/installed on button tap. */
    data class UpdateAvailable(
        val update: AppUpdate,
    ) : UpdateStatus()

    data object Downloading : UpdateStatus()

    /**
     * A newer release exists, but the OS won't let this app install it yet - see
     * `AppUpdateInstaller.canInstallPackages`. The Settings screen shows a warning whose action
     * opens the system "install unknown apps" page for this app
     * ([SettingsViewModel.onEnableSideloadingClicked]); the user is expected to tap "Update to
     * latest" again afterwards, which re-checks and proceeds automatically now that the OS allows
     * it - there's no lifecycle-based auto-recheck of this state.
     */
    data object SideloadingBlocked : UpdateStatus()

    data class Failed(
        val message: String,
    ) : UpdateStatus()
}
