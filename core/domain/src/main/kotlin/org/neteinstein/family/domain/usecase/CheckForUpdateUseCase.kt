package org.neteinstein.family.domain.usecase

import org.neteinstein.family.domain.model.UpdateCheckResult
import org.neteinstein.family.domain.repository.UpdateRepository

/** Checks GitHub Releases for a newer build than the one currently installed. */
class CheckForUpdateUseCase(
    private val updateRepository: UpdateRepository,
) {
    suspend operator fun invoke(): Result<UpdateCheckResult> = updateRepository.checkForUpdate()
}
