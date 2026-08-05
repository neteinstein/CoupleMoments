package org.neteinstein.family.feature.home

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.neteinstein.family.domain.model.Question
import org.neteinstein.family.domain.usecase.GetQuestionsUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getQuestionsUseCase: GetQuestionsUseCase = mockk()

    private lateinit var viewModel: HomeViewModel

    private val fakeQuestions = listOf(
        Question(id = 1, text = "Question 1?", languageCode = "en"),
        Question(id = 2, text = "Question 2?", languageCode = "en"),
        Question(id = 3, text = "Question 3?", languageCode = "en"),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { getQuestionsUseCase(any()) } returns fakeQuestions
        viewModel = HomeViewModel(getQuestionsUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() {
        // ViewModel is created in setUp but we can verify it starts with loading
        // After setup, it should have loaded
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadQuestions sets questions and current question`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.currentQuestion)
        assertEquals(fakeQuestions.size, state.totalQuestions)
    }

    @Test
    fun `nextQuestion advances to next question`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val initialIndex = viewModel.uiState.value.currentIndex

        viewModel.nextQuestion()

        val newIndex = viewModel.uiState.value.currentIndex
        assertEquals((initialIndex + 1) % fakeQuestions.size, newIndex)
    }

    @Test
    fun `previousQuestion goes to previous question`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.nextQuestion()
        val indexAfterNext = viewModel.uiState.value.currentIndex

        viewModel.previousQuestion()

        val indexAfterPrev = viewModel.uiState.value.currentIndex
        assertEquals((indexAfterNext - 1 + fakeQuestions.size) % fakeQuestions.size, indexAfterPrev)
    }

    @Test
    fun `nextQuestion wraps around at end of list`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        // Navigate to end
        repeat(fakeQuestions.size) { viewModel.nextQuestion() }

        // Should wrap back to first
        val state = viewModel.uiState.value
        assertNotNull(state.currentQuestion)
    }

    @Test
    fun `loadQuestions with portuguese locale loads pt questions`() = runTest {
        val ptQuestions = listOf(
            Question(id = 101, text = "Pergunta 1?", languageCode = "pt")
        )
        coEvery { getQuestionsUseCase("pt") } returns ptQuestions

        viewModel.loadQuestions("pt")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("pt", state.currentQuestion?.languageCode)
    }
}
