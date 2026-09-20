package org.neteinstein.couples.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.neteinstein.couples.data.local.CardDao
import org.neteinstein.couples.data.local.CardEntity
import org.neteinstein.couples.data.local.SeedMetadataDao
import org.neteinstein.couples.data.local.SeedMetadataEntity
import org.neteinstein.couples.data.source.QuestionSeedData

class QuestionRepositoryImplTest {
    private val cardDao: CardDao = mockk()
    private val seedMetadataDao: SeedMetadataDao = mockk()
    private lateinit var repository: QuestionRepositoryImpl

    private val englishCards =
        listOf(
            CardEntity(id = 1, text = "English question?", languageCode = "en", category = "ice_breakers"),
        )
    private val portugueseCards =
        listOf(
            CardEntity(id = 101, text = "Pergunta em português?", languageCode = "pt", category = "ice_breakers"),
        )

    @Before
    fun setUp() {
        coEvery { seedMetadataDao.getVersion() } returns null
        coEvery { seedMetadataDao.setVersion(any()) } returns Unit
        coEvery { cardDao.getHiddenIds() } returns emptyList()
        coEvery { cardDao.deleteAll() } returns Unit
        coEvery { cardDao.insertAll(any()) } returns Unit
        coEvery { cardDao.getCardsForLanguage("en") } returns englishCards
        coEvery { cardDao.getCardsForLanguage("pt") } returns portugueseCards
        repository = QuestionRepositoryImpl(cardDao, seedMetadataDao)
    }

    @Test
    fun `getQuestions returns english questions for en locale`() =
        runTest {
            val questions = repository.getQuestions("en")

            assertTrue(questions.isNotEmpty())
            assertTrue(questions.all { it.languageCode == "en" })
        }

    @Test
    fun `getQuestions returns portuguese questions for pt locale`() =
        runTest {
            val questions = repository.getQuestions("pt")

            assertTrue(questions.isNotEmpty())
            assertTrue(questions.all { it.languageCode == "pt" })
        }

    @Test
    fun `getRandomQuestion returns a question`() =
        runTest {
            val question = repository.getRandomQuestion("en")

            assertNotNull(question)
        }

    @Test
    fun `getRandomQuestion returns portuguese question for pt locale`() =
        runTest {
            val question = repository.getRandomQuestion("pt")

            assertNotNull(question)
            assertTrue(question?.languageCode == "pt")
        }

    @Test
    fun `seeds the database from seed data only once per process lifetime`() =
        runTest {
            repository.getQuestions("en")
            repository.getQuestions("en")

            coVerify(exactly = 1) { cardDao.deleteAll() }
            coVerify(exactly = 1) { cardDao.insertAll(any()) }
            coVerify(exactly = 1) { seedMetadataDao.setVersion(any()) }
        }

    @Test
    fun `replaces the database when the stored seed version differs from the current version`() =
        runTest {
            coEvery { seedMetadataDao.getVersion() } returns QuestionSeedData.VERSION + 1

            repository.getQuestions("en")

            coVerify(exactly = 1) { cardDao.deleteAll() }
            coVerify(exactly = 1) { cardDao.insertAll(any()) }
            coVerify { seedMetadataDao.setVersion(SeedMetadataEntity(version = QuestionSeedData.VERSION)) }
        }

    @Test
    fun `does not replace the database when the stored version already matches`() =
        runTest {
            coEvery { seedMetadataDao.getVersion() } returns QuestionSeedData.VERSION

            repository.getQuestions("en")

            coVerify(exactly = 0) { cardDao.deleteAll() }
            coVerify(exactly = 0) { cardDao.insertAll(any()) }
            coVerify(exactly = 0) { seedMetadataDao.setVersion(any()) }
        }

    @Test
    fun `preserves already-hidden card ids across a version-triggered replace`() =
        runTest {
            val hiddenId = QuestionSeedData.all.first().id
            coEvery { cardDao.getHiddenIds() } returns listOf(hiddenId)
            val insertedCards = slot<List<CardEntity>>()
            coEvery { cardDao.insertAll(capture(insertedCards)) } returns Unit

            repository.getQuestions("en")

            val reinsertedHiddenCard = insertedCards.captured.first { it.id == hiddenId }
            val reinsertedOtherCard = insertedCards.captured.first { it.id != hiddenId }
            assertTrue(reinsertedHiddenCard.isHidden)
            assertTrue(!reinsertedOtherCard.isHidden)
        }

    @Test
    fun `replace reinserts every seed card`() =
        runTest {
            val insertedCards = slot<List<CardEntity>>()
            coEvery { cardDao.insertAll(capture(insertedCards)) } returns Unit

            repository.getQuestions("en")

            assertEquals(QuestionSeedData.all.size, insertedCards.captured.size)
        }
}
