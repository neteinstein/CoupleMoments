package org.neteinstein.family.domain.usecase

import org.neteinstein.family.domain.repository.UpdateRepository

/**
 * Deletes a previously-downloaded update APK (see [DownloadAppUpdateUseCase]) once it's no longer
 * needed, so it doesn't sit in the cache directory taking up space indefinitely.
 */
class ClearDownloadedUpdateUseCase(
    private val updateRepository: UpdateRepository,
) {
    suspend operator fun invoke(): Result<Unit> = updateRepository.clearDownloadedUpdate()
}
