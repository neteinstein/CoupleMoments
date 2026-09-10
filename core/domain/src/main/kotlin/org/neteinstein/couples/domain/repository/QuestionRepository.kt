package org.neteinstein.couples.domain.repository

import org.neteinstein.couples.domain.model.Question

interface QuestionRepository {
    suspend fun getQuestions(languageCode: String): List<Question>

    suspend fun getRandomQuestion(languageCode: String): Question?
}
