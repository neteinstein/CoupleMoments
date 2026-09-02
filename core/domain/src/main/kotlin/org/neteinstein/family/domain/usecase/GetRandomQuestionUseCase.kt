package org.neteinstein.family.domain.usecase

import org.neteinstein.family.domain.model.Question
import org.neteinstein.family.domain.repository.QuestionRepository

class GetRandomQuestionUseCase(
    private val questionRepository: QuestionRepository,
) {
    suspend operator fun invoke(languageCode: String): Question? = questionRepository.getRandomQuestion(languageCode)
}
