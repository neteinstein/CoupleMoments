package org.neteinstein.couples.domain.repository

import org.neteinstein.couples.domain.model.AppUpdate
import org.neteinstein.couples.domain.model.UpdateCheckResult

/** Boundary between the update-check domain layer and GitHub Releases. */
interface UpdateRepository {
    /** Compares the installed build's version against GitHub's latest release. */
    suspend fun checkForUpdate(): Result<UpdateCheckResult>

    /** Downloads [update]'s APK, returning its raw bytes. */
    suspend fun downloadUpdate(update: AppUpdate): Result<ByteArray>

    /**
     * Deletes any APK previously written by [downloadUpdate] - a no-op, not a failure, if
     * nothing was downloaded.
     */
    suspend fun clearDownloadedUpdate(): Result<Unit>
}
