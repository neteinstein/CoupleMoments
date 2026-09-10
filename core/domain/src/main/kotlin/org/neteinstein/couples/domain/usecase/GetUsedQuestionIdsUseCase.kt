package org.neteinstein.couples.domain.usecase

import org.neteinstein.couples.domain.repository.UsedQuestionsRepository

class GetUsedQuestionIdsUseCase(
    private val usedQuestionsRepository: UsedQuestionsRepository,
) {
    suspend operator fun invoke(): Set<Int> = usedQuestionsRepository.getUsedQuestionIds()
}
