package org.neteinstein.family.domain.repository

import org.neteinstein.family.domain.model.Question

interface QuestionRepository {
    suspend fun getQuestions(languageCode: String): List<Question>
    suspend fun getRandomQuestion(languageCode: String): Question?
}
