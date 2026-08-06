package org.neteinstein.family.domain.usecase

import org.neteinstein.family.domain.repository.UsedQuestionsRepository

class ResetUsedQuestionsUseCase(private val usedQuestionsRepository: UsedQuestionsRepository) {
    suspend operator fun invoke() = usedQuestionsRepository.resetUsedQuestions()
}
