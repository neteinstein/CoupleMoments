package org.neteinstein.family.data.repository

import org.neteinstein.family.data.source.QuestionDataSource
import org.neteinstein.family.domain.model.Question
import org.neteinstein.family.domain.repository.QuestionRepository

class QuestionRepositoryImpl(private val dataSource: QuestionDataSource = QuestionDataSource) : QuestionRepository {

    override suspend fun getQuestions(languageCode: String): List<Question> = dataSource.getQuestions(languageCode)

    override suspend fun getRandomQuestion(languageCode: String): Question? = dataSource.getQuestions(languageCode).randomOrNull()
}
