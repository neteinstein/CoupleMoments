package org.neteinstein.couples.domain.usecase

import org.neteinstein.couples.domain.repository.UsedQuestionsRepository

class ResetUsedQuestionsUseCase(
    private val usedQuestionsRepository: UsedQuestionsRepository,
) {
    suspend operator fun invoke() = usedQuestionsRepository.resetUsedQuestions()
}
