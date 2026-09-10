package org.neteinstein.couples.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.neteinstein.couples.domain.model.Question
import org.neteinstein.couples.domain.repository.QuestionRepository

class GetRandomQuestionUseCaseTest {
    private val repository: QuestionRepository = mockk()
    private val useCase = GetRandomQuestionUseCase(repository)

    @Test
    fun `invoke returns question from repository`() =
        runTest {
            val question = Question(id = 1, text = "Test question?", languageCode = "en")
            coEvery { repository.getRandomQuestion("en") } returns question

            val result = useCase("en")

            assertEquals(question, result)
        }

    @Test
    fun `invoke returns null when no questions available`() =
        runTest {
            coEvery { repository.getRandomQuestion("en") } returns null

            val result = useCase("en")

            assertNull(result)
        }

    @Test
    fun `invoke passes language code to repository`() =
        runTest {
            coEvery { repository.getRandomQuestion("pt") } returns null

            useCase("pt")

            io.mockk.coVerify { repository.getRandomQuestion("pt") }
        }
}
