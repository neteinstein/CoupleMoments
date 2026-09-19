package org.neteinstein.couples.domain.usecase

import org.neteinstein.couples.domain.model.UpdateCheckResult
import org.neteinstein.couples.domain.repository.UpdateRepository

/** Checks GitHub Releases for a newer build than the one currently installed. */
class CheckForUpdateUseCase(
    private val updateRepository: UpdateRepository,
) {
    suspend operator fun invoke(): Result<UpdateCheckResult> = updateRepository.checkForUpdate()
}
