package org.neteinstein.couples.feature.game

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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.neteinstein.couples.domain.repository.LocaleProvider

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val localeProvider: LocaleProvider = mockk()

    private lateinit var viewModel: GameViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { localeProvider.currentLanguageCode() } returns "en"
        viewModel = GameViewModel(localeProvider)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads the full english question set`() {
        val state = viewModel.uiState.value
        assertNotNull(state.currentQuestion)
        assertEquals(GameQuestions.forLanguage("en").size, state.totalQuestions)
        assertEquals(0, state.currentIndex)
    }

    @Test
    fun `init loads questions in the app's currently applied language`() {
        every { localeProvider.currentLanguageCode() } returns "pt"

        val ptViewModel = GameViewModel(localeProvider)

        val ptTexts = GameQuestions.forLanguage("pt").map { it.text }
        assertTrue(
            ptViewModel.uiState.value.currentQuestion
                ?.text in ptTexts,
        )
    }

    @Test
    fun `nextQuestion advances to next question and wraps around`() {
        val total = viewModel.uiState.value.totalQuestions

        repeat(total) { viewModel.nextQuestion() }

        assertEquals(0, viewModel.uiState.value.currentIndex)
    }

    @Test
    fun `previousQuestion goes to previous question and wraps around`() {
        val total = viewModel.uiState.value.totalQuestions

        viewModel.previousQuestion()

        assertEquals(total - 1, viewModel.uiState.value.currentIndex)
    }

    @Test
    fun `onScreenEntered reshuffles from the new language's set when the app language changed`() =
        runTest {
            every { localeProvider.currentLanguageCode() } returns "es"

            viewModel.onScreenEntered()

            val esTexts = GameQuestions.forLanguage("es").map { it.text }
            assertTrue(
                viewModel.uiState.value.currentQuestion
                    ?.text in esTexts,
            )
        }

    @Test
    fun `onScreenEntered does nothing when the app language is unchanged`() =
        runTest {
            val questionsBefore = viewModel.uiState.value.questions

            viewModel.onScreenEntered()

            assertEquals(questionsBefore, viewModel.uiState.value.questions)
        }

    @Test
    fun `unsupported language code falls back to english`() {
        every { localeProvider.currentLanguageCode() } returns "xx"

        val fallbackViewModel = GameViewModel(localeProvider)

        val enTexts = GameQuestions.forLanguage("en").map { it.text }
        assertTrue(
            fallbackViewModel.uiState.value.currentQuestion
                ?.text in enTexts,
        )
    }

    @Test
    fun `game questions are consistent across all supported languages`() {
        val expectedIds = GameQuestions.forLanguage("en").map { it.id }.toSet()
        for (languageCode in listOf("en", "pt", "es", "de", "fr")) {
            val ids = GameQuestions.forLanguage(languageCode).map { it.id }.toSet()
            assertEquals("Question ids for '$languageCode' should match 'en'", expectedIds, ids)
        }
    }
}
