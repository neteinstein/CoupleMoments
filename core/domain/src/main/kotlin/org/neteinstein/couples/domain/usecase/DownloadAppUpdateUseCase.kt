package org.neteinstein.couples.domain.usecase

import org.neteinstein.couples.domain.model.AppUpdate
import org.neteinstein.couples.domain.repository.UpdateRepository
import java.io.File

/**
 * Downloads the APK for a previously-found [AppUpdate] to local storage, ready to be handed to
 * [org.neteinstein.couples.domain.repository.AppUpdateInstaller]. Kept as its own use case (rather
 * than folded into [CheckForUpdateUseCase]) so a caller can gate the download behind a
 * sideloading-permission check in between the two calls.
 */
class DownloadAppUpdateUseCase(
    private val updateRepository: UpdateRepository,
) {
    suspend operator fun invoke(update: AppUpdate): Result<File> = updateRepository.downloadUpdate(update)
}
