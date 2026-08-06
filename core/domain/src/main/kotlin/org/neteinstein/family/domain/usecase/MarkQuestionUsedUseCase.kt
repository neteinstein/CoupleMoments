package org.neteinstein.family.domain.usecase

import org.neteinstein.family.domain.repository.UsedQuestionsRepository

class MarkQuestionUsedUseCase(private val usedQuestionsRepository: UsedQuestionsRepository) {
    suspend operator fun invoke(questionId: Int) = usedQuestionsRepository.markAsUsed(questionId)
}
