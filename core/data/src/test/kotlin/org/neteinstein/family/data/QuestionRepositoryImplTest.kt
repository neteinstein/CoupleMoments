package org.neteinstein.family.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionRepositoryImplTest {

    private val repository = QuestionRepositoryImpl()

    @Test
    fun `getQuestions returns english questions for en locale`() = runTest {
        val questions = repository.getQuestions("en")

        assertTrue(questions.isNotEmpty())
        assertTrue(questions.all { it.languageCode == "en" })
    }

    @Test
    fun `getQuestions returns portuguese questions for pt locale`() = runTest {
        val questions = repository.getQuestions("pt")

        assertTrue(questions.isNotEmpty())
        assertTrue(questions.all { it.languageCode == "pt" })
    }

    @Test
    fun `getQuestions defaults to english for unknown locale`() = runTest {
        val questions = repository.getQuestions("it")

        assertTrue(questions.isNotEmpty())
        assertTrue(questions.all { it.languageCode == "en" })
    }

    @Test
    fun `getRandomQuestion returns a question`() = runTest {
        val question = repository.getRandomQuestion("en")

        assertNotNull(question)
    }

    @Test
    fun `getRandomQuestion returns portuguese question for pt locale`() = runTest {
        val question = repository.getRandomQuestion("pt")

        assertNotNull(question)
        assertTrue(question?.languageCode == "pt")
    }
}
