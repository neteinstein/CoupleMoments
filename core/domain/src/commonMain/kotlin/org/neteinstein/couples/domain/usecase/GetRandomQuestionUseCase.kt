package org.neteinstein.couples.domain.usecase

import org.neteinstein.couples.domain.model.Question
import org.neteinstein.couples.domain.repository.QuestionRepository

class GetRandomQuestionUseCase(
    private val questionRepository: QuestionRepository,
) {
    suspend operator fun invoke(languageCode: String): Question? = questionRepository.getRandomQuestion(languageCode)
}
