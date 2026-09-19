package org.neteinstein.couples.domain.usecase

import org.neteinstein.couples.domain.repository.QuestionsForParentsRepository

class SetQuestionsForParentsEnabledUseCase(
    private val questionsForParentsRepository: QuestionsForParentsRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = questionsForParentsRepository.setEnabled(enabled)
}
