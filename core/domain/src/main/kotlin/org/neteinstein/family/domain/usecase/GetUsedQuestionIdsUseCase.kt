package org.neteinstein.family.domain.usecase

import org.neteinstein.family.domain.repository.UsedQuestionsRepository

class GetUsedQuestionIdsUseCase(
    private val usedQuestionsRepository: UsedQuestionsRepository,
) {
    suspend operator fun invoke(): Set<Int> = usedQuestionsRepository.getUsedQuestionIds()
}
