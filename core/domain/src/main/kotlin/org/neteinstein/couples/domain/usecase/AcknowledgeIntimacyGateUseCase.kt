package org.neteinstein.couples.domain.usecase

import org.neteinstein.couples.domain.repository.IntimacyGateRepository

class AcknowledgeIntimacyGateUseCase(
    private val intimacyGateRepository: IntimacyGateRepository,
) {
    suspend operator fun invoke() = intimacyGateRepository.setAcknowledged()
}
