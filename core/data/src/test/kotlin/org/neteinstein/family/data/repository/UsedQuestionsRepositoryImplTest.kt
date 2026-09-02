package org.neteinstein.family.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.neteinstein.family.data.local.CardDao

class UsedQuestionsRepositoryImplTest {
    private val cardDao: CardDao = mockk()

    private lateinit var repository: UsedQuestionsRepositoryImpl

    @Before
    fun setUp() {
        repository = UsedQuestionsRepositoryImpl(cardDao)
    }

    @Test
    fun `getUsedQuestionIds returns empty set when nothing is hidden`() =
        runTest {
            coEvery { cardDao.getHiddenIds() } returns emptyList()

            assertEquals(emptySet<Int>(), repository.getUsedQuestionIds())
        }

    @Test
    fun `getUsedQuestionIds returns the hidden ids from the database`() =
        runTest {
            coEvery { cardDao.getHiddenIds() } returns listOf(1, 2, 3)

            assertEquals(setOf(1, 2, 3), repository.getUsedQuestionIds())
        }

    @Test
    fun `markAsUsed hides the card in the database`() =
        runTest {
            coEvery { cardDao.markHidden(2) } returns Unit

            repository.markAsUsed(2)

            coVerify { cardDao.markHidden(2) }
        }

    @Test
    fun `resetUsedQuestions clears every hidden card`() =
        runTest {
            coEvery { cardDao.resetAllHidden() } returns Unit

            repository.resetUsedQuestions()

            coVerify { cardDao.resetAllHidden() }
        }
}
