package org.neteinstein.couples.domain.usecase

import org.neteinstein.couples.domain.repository.UpdateRepository

/**
 * Deletes a previously-downloaded update APK (see [DownloadAppUpdateUseCase]) once it's no longer
 * needed, so it doesn't sit in the cache directory taking up space indefinitely.
 */
class ClearDownloadedUpdateUseCase(
    private val updateRepository: UpdateRepository,
) {
    suspend operator fun invoke(): Result<Unit> = updateRepository.clearDownloadedUpdate()
}
