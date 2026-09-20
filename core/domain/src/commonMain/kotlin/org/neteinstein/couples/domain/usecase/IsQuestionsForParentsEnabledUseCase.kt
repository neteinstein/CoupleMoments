package org.neteinstein.couples.domain.usecase

import org.neteinstein.couples.domain.repository.QuestionsForParentsRepository

class IsQuestionsForParentsEnabledUseCase(
    private val questionsForParentsRepository: QuestionsForParentsRepository,
) {
    suspend operator fun invoke(): Boolean = questionsForParentsRepository.isEnabled()
}
