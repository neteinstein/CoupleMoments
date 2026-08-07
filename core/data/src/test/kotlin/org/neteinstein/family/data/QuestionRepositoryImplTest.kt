package org.neteinstein.family.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.neteinstein.family.data.local.CardDao
import org.neteinstein.family.data.local.CardEntity

class QuestionRepositoryImplTest {

    private val cardDao: CardDao = mockk()
    private lateinit var repository: QuestionRepositoryImpl

    private val englishCards = listOf(
        CardEntity(id = 1, text = "English question?", languageCode = "en", category = "ice_breakers")
    )
    private val portugueseCards = listOf(
        CardEntity(id = 101, text = "Pergunta em português?", languageCode = "pt", category = "ice_breakers")
    )

    @Before
    fun setUp() {
        coEvery { cardDao.insertAll(any()) } returns Unit
        coEvery { cardDao.getCardsForLanguage("en") } returns englishCards
        coEvery { cardDao.getCardsForLanguage("pt") } returns portugueseCards
        repository = QuestionRepositoryImpl(cardDao)
    }

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

    @Test
    fun `seeds the database from seed data only once per process lifetime`() = runTest {
        repository.getQuestions("en")
        repository.getQuestions("en")

        coVerify(exactly = 1) { cardDao.insertAll(any()) }
    }

    @Test
    fun `seeds the database even when it already has cards, so seed data added later is not skipped`() = runTest {
        repository.getQuestions("en")

        coVerify(exactly = 1) { cardDao.insertAll(any()) }
    }
}
