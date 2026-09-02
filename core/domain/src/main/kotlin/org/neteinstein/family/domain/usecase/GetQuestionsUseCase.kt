package org.neteinstein.family.domain.usecase

import org.neteinstein.family.domain.model.Question
import org.neteinstein.family.domain.repository.QuestionRepository

class GetQuestionsUseCase(
    private val questionRepository: QuestionRepository,
) {
    suspend operator fun invoke(languageCode: String): List<Question> = questionRepository.getQuestions(languageCode)
}
