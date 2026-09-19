package org.neteinstein.couples.domain.usecase

import org.neteinstein.couples.domain.repository.UsedQuestionsRepository

class MarkQuestionUsedUseCase(
    private val usedQuestionsRepository: UsedQuestionsRepository,
) {
    suspend operator fun invoke(questionId: Int) = usedQuestionsRepository.markAsUsed(questionId)
}
