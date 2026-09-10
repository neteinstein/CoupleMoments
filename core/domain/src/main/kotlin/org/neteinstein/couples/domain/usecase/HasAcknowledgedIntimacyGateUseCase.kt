package org.neteinstein.couples.domain.usecase

import org.neteinstein.couples.domain.repository.IntimacyGateRepository

class HasAcknowledgedIntimacyGateUseCase(
    private val intimacyGateRepository: IntimacyGateRepository,
) {
    suspend operator fun invoke(): Boolean = intimacyGateRepository.hasAcknowledged()
}
