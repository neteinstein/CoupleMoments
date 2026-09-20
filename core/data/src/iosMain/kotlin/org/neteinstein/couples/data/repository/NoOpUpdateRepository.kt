package org.neteinstein.couples.data.repository

import org.neteinstein.couples.domain.model.AppUpdate
import org.neteinstein.couples.domain.model.UpdateCheckResult
import org.neteinstein.couples.domain.repository.UpdateRepository

/**
 * Self-update doesn't exist on iOS (Apple does not allow an app to install another build of itself), so this exists purely so Koin can satisfy
 * [UpdateRepository] injection. `feature:settings` never calls it: its "Updates" section is
 * hidden whenever the `updatesEnabled` DI flag is false, which it always is off Android.
 */
class NoOpUpdateRepository : UpdateRepository {
    override suspend fun checkForUpdate(): Result<UpdateCheckResult> =
        Result.failure(UnsupportedOperationException("Self-update is not supported on this platform"))

    override suspend fun downloadUpdate(update: AppUpdate): Result<ByteArray> =
        Result.failure(UnsupportedOperationException("Self-update is not supported on this platform"))

    override suspend fun clearDownloadedUpdate(): Result<Unit> = Result.success(Unit)
}
