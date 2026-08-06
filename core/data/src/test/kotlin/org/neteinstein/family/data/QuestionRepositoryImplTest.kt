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
        coEvery { cardDao.count() } returns 1
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
    fun `seeds the database from seed data only once when empty`() = runTest {
        coEvery { cardDao.count() } returns 0
        coEvery { cardDao.insertAll(any()) } returns Unit

        repository.getQuestions("en")
        repository.getQuestions("en")

        coVerify(exactly = 1) { cardDao.insertAll(any()) }
    }

    @Test
    fun `does not seed the database when it already has cards`() = runTest {
        coEvery { cardDao.count() } returns 1

        repository.getQuestions("en")

        coVerify(exactly = 0) { cardDao.insertAll(any()) }
    }
}
