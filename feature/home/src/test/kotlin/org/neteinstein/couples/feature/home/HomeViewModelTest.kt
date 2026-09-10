package org.neteinstein.couples.feature.home

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
import org.neteinstein.couples.domain.model.Question
import org.neteinstein.couples.domain.model.QuestionCategory
import org.neteinstein.couples.domain.repository.LocaleProvider
import org.neteinstein.couples.domain.usecase.AcknowledgeIntimacyGateUseCase
import org.neteinstein.couples.domain.usecase.GetQuestionsUseCase
import org.neteinstein.couples.domain.usecase.GetUsedQuestionIdsUseCase
import org.neteinstein.couples.domain.usecase.HasAcknowledgedIntimacyGateUseCase
import org.neteinstein.couples.domain.usecase.MarkQuestionUsedUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val getQuestionsUseCase: GetQuestionsUseCase = mockk()
    private val localeProvider: LocaleProvider = mockk()
    private val getUsedQuestionIdsUseCase: GetUsedQuestionIdsUseCase = mockk()
    private val markQuestionUsedUseCase: MarkQuestionUsedUseCase = mockk()
    private val hasAcknowledgedIntimacyGateUseCase: HasAcknowledgedIntimacyGateUseCase = mockk()
    private val acknowledgeIntimacyGateUseCase: AcknowledgeIntimacyGateUseCase = mockk()

    private lateinit var viewModel: HomeViewModel

    private val fakeQuestions =
        listOf(
            Question(id = 1, text = "Question 1?", languageCode = "en"),
            Question(id = 2, text = "Question 2?", languageCode = "en"),
            Question(id = 3, text = "Question 3?", languageCode = "en"),
        )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { localeProvider.currentLanguageCode() } returns "en"
        coEvery { getQuestionsUseCase(any()) } returns fakeQuestions
        coEvery { getUsedQuestionIdsUseCase() } returns emptySet()
        coEvery { markQuestionUsedUseCase(any()) } returns Unit
        coEvery { hasAcknowledgedIntimacyGateUseCase() } returns true
        coEvery { acknowledgeIntimacyGateUseCase() } returns Unit
        viewModel =
            HomeViewModel(
                getQuestionsUseCase,
                localeProvider,
                getUsedQuestionIdsUseCase,
                markQuestionUsedUseCase,
                hasAcknowledgedIntimacyGateUseCase,
                acknowledgeIntimacyGateUseCase,
            )
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
    fun `loadQuestions sets questions and current question`() =
        runTest {
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertNotNull(state.currentQuestion)
            assertEquals(fakeQuestions.size, state.totalQuestions)
        }

    @Test
    fun `nextQuestion advances to next question`() =
        runTest {
            testDispatcher.scheduler.advanceUntilIdle()
            val initialIndex = viewModel.uiState.value.currentIndex

            viewModel.nextQuestion()

            val newIndex = viewModel.uiState.value.currentIndex
            assertEquals((initialIndex + 1) % fakeQuestions.size, newIndex)
        }

    @Test
    fun `previousQuestion goes to previous question`() =
        runTest {
            testDispatcher.scheduler.advanceUntilIdle()
            viewModel.nextQuestion()
            val indexAfterNext = viewModel.uiState.value.currentIndex

            viewModel.previousQuestion()

            val indexAfterPrev = viewModel.uiState.value.currentIndex
            assertEquals((indexAfterNext - 1 + fakeQuestions.size) % fakeQuestions.size, indexAfterPrev)
        }

    @Test
    fun `nextQuestion wraps around at end of list`() =
        runTest {
            testDispatcher.scheduler.advanceUntilIdle()
            // Navigate to end
            repeat(fakeQuestions.size) { viewModel.nextQuestion() }

            // Should wrap back to first
            val state = viewModel.uiState.value
            assertNotNull(state.currentQuestion)
        }

    @Test
    fun `loadQuestions with portuguese locale loads pt questions`() =
        runTest {
            val ptQuestions =
                listOf(
                    Question(id = 101, text = "Pergunta 1?", languageCode = "pt"),
                )
            coEvery { getQuestionsUseCase("pt") } returns ptQuestions

            viewModel.loadQuestions("pt")
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals("pt", state.currentQuestion?.languageCode)
        }

    @Test
    fun `init loads questions in the app's currently applied language, not a hardcoded default`() =
        runTest {
            // Regression test: the ViewModel used to always default to "en" on startup regardless of
            // the language configured for the app (see LocaleProvider), silently ignoring whatever the
            // user picked via Settings > App Language.
            every { localeProvider.currentLanguageCode() } returns "pt"
            val ptQuestions = listOf(Question(id = 101, text = "Pergunta 1?", languageCode = "pt"))
            coEvery { getQuestionsUseCase("pt") } returns ptQuestions

            val ptViewModel =
                HomeViewModel(
                    getQuestionsUseCase,
                    localeProvider,
                    getUsedQuestionIdsUseCase,
                    markQuestionUsedUseCase,
                    hasAcknowledgedIntimacyGateUseCase,
                    acknowledgeIntimacyGateUseCase,
                )
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(
                "pt",
                ptViewModel.uiState.value.currentQuestion
                    ?.languageCode,
            )
            coVerify(exactly = 1) { getQuestionsUseCase("pt") }
        }

    @Test
    fun `onScreenEntered reloads questions when the app language changed since the last load`() =
        runTest {
            testDispatcher.scheduler.advanceUntilIdle()
            val esQuestions = listOf(Question(id = 201, text = "Pregunta 1?", languageCode = "es"))
            coEvery { getQuestionsUseCase("es") } returns esQuestions

            // Simulates the user changing the per-app language in system Settings and returning to Home.
            every { localeProvider.currentLanguageCode() } returns "es"
            viewModel.onScreenEntered()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(
                "es",
                viewModel.uiState.value.currentQuestion
                    ?.languageCode,
            )
        }

    @Test
    fun `onScreenEntered does nothing when the app language is unchanged`() =
        runTest {
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onScreenEntered()
            testDispatcher.scheduler.advanceUntilIdle()

            // Only the initial load from init() should have happened - no redundant reload for "en".
            coVerify(exactly = 1) { getQuestionsUseCase("en") }
        }

    @Test
    fun `onScreenEntered refreshes hidden cards when the app language is unchanged`() =
        runTest {
            // Regression test: returning to Home from Settings after "Reset Cards" (or after hiding a
            // card elsewhere) should bring previously hidden cards back without needing a full reload.
            testDispatcher.scheduler.advanceUntilIdle()
            coEvery { getUsedQuestionIdsUseCase() } returns setOf(2)

            viewModel.onScreenEntered()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(fakeQuestions.size - 1, state.totalQuestions)
            coVerify(exactly = 1) { getQuestionsUseCase("en") }
        }

    @Test
    fun `onCategorySelected filters questions to the chosen category`() =
        runTest {
            val categorizedQuestions =
                listOf(
                    Question(id = 1, text = "Q1?", languageCode = "en", category = QuestionCategory.Memories),
                    Question(id = 2, text = "Q2?", languageCode = "en", category = QuestionCategory.Values),
                    Question(id = 3, text = "Q3?", languageCode = "en", category = QuestionCategory.Memories),
                )
            coEvery { getQuestionsUseCase("en") } returns categorizedQuestions
            viewModel.loadQuestions("en")
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onCategorySelected(QuestionCategory.Memories)

            val state = viewModel.uiState.value
            assertEquals(QuestionCategory.Memories, state.selectedCategory)
            assertEquals(2, state.totalQuestions)
            assertEquals(QuestionCategory.Memories, state.currentQuestion?.category)
            assertEquals(0, state.currentIndex)
        }

    @Test
    fun `onCategorySelected with null shows all questions again`() =
        runTest {
            val categorizedQuestions =
                listOf(
                    Question(id = 1, text = "Q1?", languageCode = "en", category = QuestionCategory.Memories),
                    Question(id = 2, text = "Q2?", languageCode = "en", category = QuestionCategory.Values),
                )
            coEvery { getQuestionsUseCase("en") } returns categorizedQuestions
            viewModel.loadQuestions("en")
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onCategorySelected(QuestionCategory.Values)
            viewModel.onCategorySelected(null)

            val state = viewModel.uiState.value
            assertEquals(null, state.selectedCategory)
            assertEquals(categorizedQuestions.size, state.totalQuestions)
        }

    @Test
    fun `onCategorySelected with Intimacy shows the gate instead of switching when not yet acknowledged`() =
        runTest {
            coEvery { hasAcknowledgedIntimacyGateUseCase() } returns false
            testDispatcher.scheduler.advanceUntilIdle()
            val categoryBefore = viewModel.uiState.value.selectedCategory

            viewModel.onCategorySelected(QuestionCategory.Intimacy)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(true, state.showIntimacyGate)
            assertEquals(categoryBefore, state.selectedCategory)
            coVerify(exactly = 0) { acknowledgeIntimacyGateUseCase() }
        }

    @Test
    fun `onCategorySelected with Intimacy switches immediately once already acknowledged`() =
        runTest {
            coEvery { hasAcknowledgedIntimacyGateUseCase() } returns true
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onCategorySelected(QuestionCategory.Intimacy)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.showIntimacyGate)
            assertEquals(QuestionCategory.Intimacy, state.selectedCategory)
        }

    @Test
    fun `onIntimacyGateConfirmed persists acknowledgement, hides the gate, and switches category`() =
        runTest {
            coEvery { hasAcknowledgedIntimacyGateUseCase() } returns false
            viewModel.onCategorySelected(QuestionCategory.Intimacy)
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onIntimacyGateConfirmed()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.showIntimacyGate)
            assertEquals(QuestionCategory.Intimacy, state.selectedCategory)
            coVerify(exactly = 1) { acknowledgeIntimacyGateUseCase() }
        }

    @Test
    fun `onIntimacyGateDismissed hides the gate without changing the selected category`() =
        runTest {
            coEvery { hasAcknowledgedIntimacyGateUseCase() } returns false
            viewModel.onCategorySelected(QuestionCategory.Memories)
            viewModel.onCategorySelected(QuestionCategory.Intimacy)
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onIntimacyGateDismissed()

            val state = viewModel.uiState.value
            assertFalse(state.showIntimacyGate)
            assertEquals(QuestionCategory.Memories, state.selectedCategory)
            coVerify(exactly = 0) { acknowledgeIntimacyGateUseCase() }
        }

    @Test
    fun `markCurrentQuestionAsUsed persists the id and removes it from rotation`() =
        runTest {
            testDispatcher.scheduler.advanceUntilIdle()
            val current = viewModel.uiState.value.currentQuestion
            requireNotNull(current)

            viewModel.markCurrentQuestionAsUsed()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 1) { markQuestionUsedUseCase(current.id) }
            val state = viewModel.uiState.value
            assertEquals(fakeQuestions.size - 1, state.totalQuestions)
            assertFalse(state.currentQuestion?.id == current.id)
        }

    @Test
    fun `loadQuestions excludes previously used questions`() =
        runTest {
            coEvery { getUsedQuestionIdsUseCase() } returns setOf(2)

            viewModel.loadQuestions("en")
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(fakeQuestions.size - 1, state.totalQuestions)
            assertFalse(state.currentQuestion?.id == 2)
        }
}
