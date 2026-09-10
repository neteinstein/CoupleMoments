package org.neteinstein.couples.feature.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.neteinstein.couples.domain.model.Question
import org.neteinstein.couples.domain.repository.LocaleProvider
import org.neteinstein.couples.domain.usecase.AcknowledgeIntimacyGateUseCase
import org.neteinstein.couples.domain.usecase.GetQuestionsUseCase
import org.neteinstein.couples.domain.usecase.GetUsedQuestionIdsUseCase
import org.neteinstein.couples.domain.usecase.HasAcknowledgedIntimacyGateUseCase
import org.neteinstein.couples.domain.usecase.MarkQuestionUsedUseCase
import org.neteinstein.couples.ui.theme.CoupleMomentsTheme
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp")
class HomeScreenGridViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeQuestions =
        listOf(
            Question(id = 1, text = "Grid question one?", languageCode = "en"),
            Question(id = 2, text = "Grid question two?", languageCode = "en"),
            Question(id = 3, text = "Grid question three?", languageCode = "en"),
        )

    private fun buildViewModel(): HomeViewModel {
        val getQuestionsUseCase: GetQuestionsUseCase = mockk()
        val localeProvider: LocaleProvider = mockk()
        val getUsedQuestionIdsUseCase: GetUsedQuestionIdsUseCase = mockk()
        val markQuestionUsedUseCase: MarkQuestionUsedUseCase = mockk()
        val hasAcknowledgedIntimacyGateUseCase: HasAcknowledgedIntimacyGateUseCase = mockk()
        val acknowledgeIntimacyGateUseCase: AcknowledgeIntimacyGateUseCase = mockk()
        every { localeProvider.currentLanguageCode() } returns "en"
        coEvery { getQuestionsUseCase(any()) } returns fakeQuestions
        coEvery { getUsedQuestionIdsUseCase() } returns emptySet()
        coEvery { markQuestionUsedUseCase(any()) } returns Unit
        coEvery { hasAcknowledgedIntimacyGateUseCase() } returns true
        coEvery { acknowledgeIntimacyGateUseCase() } returns Unit
        return HomeViewModel(
            getQuestionsUseCase,
            localeProvider,
            getUsedQuestionIdsUseCase,
            markQuestionUsedUseCase,
            hasAcknowledgedIntimacyGateUseCase,
            acknowledgeIntimacyGateUseCase,
        )
    }

    @Test
    fun `switching to grid view shows every question as a card`() {
        composeTestRule.setContent {
            CoupleMomentsTheme(darkTheme = false, dynamicColor = false) {
                HomeScreen(onSettingsClick = {}, viewModel = buildViewModel())
            }
        }

        composeTestRule.onNodeWithContentDescription("Switch to grid view").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Grid question one?").assertExists()
        composeTestRule.onNodeWithText("Grid question two?").assertExists()
        composeTestRule.onNodeWithText("Grid question three?").assertExists()
        composeTestRule.onNodeWithContentDescription("Switch to card view").assertExists()
    }

    @Test
    fun `tapping a grid card opens it full screen`() {
        composeTestRule.setContent {
            CoupleMomentsTheme(darkTheme = false, dynamicColor = false) {
                HomeScreen(onSettingsClick = {}, viewModel = buildViewModel())
            }
        }

        composeTestRule.onNodeWithContentDescription("Switch to grid view").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Grid question two?").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Close").assertExists()
    }

    @Test
    fun `switching back to card view restores the swipe hints`() {
        composeTestRule.setContent {
            CoupleMomentsTheme(darkTheme = false, dynamicColor = false) {
                HomeScreen(onSettingsClick = {}, viewModel = buildViewModel())
            }
        }

        composeTestRule.onNodeWithContentDescription("Switch to grid view").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Switch to card view").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Switch to grid view").assertExists()
    }
}
