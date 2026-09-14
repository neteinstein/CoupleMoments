package org.neteinstein.couples.feature.game

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.neteinstein.couples.domain.repository.LocaleProvider

class GameViewModelTest {
    private val localeProvider: LocaleProvider = mockk()
    private lateinit var viewModel: GameViewModel

    @Before
    fun setUp() {
        every { localeProvider.currentLanguageCode() } returns "en"
        viewModel = GameViewModel(localeProvider)
    }

    @Test
    fun `init loads the full question set for the app's currently applied language`() {
        val state = viewModel.uiState.value

        assertNotNull(state.currentQuestion)
        assertEquals(GameQuestions.forLanguage("en").size, state.totalQuestions)
        assertEquals(0, state.currentIndex)
    }

    @Test
    fun `nextQuestion advances to next question`() {
        val initialIndex = viewModel.uiState.value.currentIndex

        viewModel.nextQuestion()

        val newIndex = viewModel.uiState.value.currentIndex
        assertEquals((initialIndex + 1) % viewModel.uiState.value.totalQuestions, newIndex)
    }

    @Test
    fun `nextQuestion wraps around at end of list`() {
        val total = viewModel.uiState.value.totalQuestions
        repeat(total) { viewModel.nextQuestion() }

        assertEquals(0, viewModel.uiState.value.currentIndex)
    }

    @Test
    fun `previousQuestion goes to previous question`() {
        viewModel.nextQuestion()
        val indexAfterNext = viewModel.uiState.value.currentIndex

        viewModel.previousQuestion()

        val indexAfterPrev = viewModel.uiState.value.currentIndex
        val total = viewModel.uiState.value.totalQuestions
        assertEquals((indexAfterNext - 1 + total) % total, indexAfterPrev)
    }

    @Test
    fun `onScreenEntered reloads questions when the app language changed since the last load`() {
        every { localeProvider.currentLanguageCode() } returns "pt"

        viewModel.onScreenEntered()

        assertEquals(GameQuestions.forLanguage("pt").size, viewModel.uiState.value.totalQuestions)
    }

    @Test
    fun `onScreenEntered does nothing when the app language is unchanged`() {
        viewModel.onScreenEntered()

        verify(exactly = 2) { localeProvider.currentLanguageCode() }
    }
}
