package org.neteinstein.family.feature.home

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
import org.neteinstein.family.domain.model.Question
import org.neteinstein.family.domain.repository.LocaleProvider
import org.neteinstein.family.domain.usecase.GetQuestionsUseCase
import org.neteinstein.family.domain.usecase.GetUsedQuestionIdsUseCase
import org.neteinstein.family.domain.usecase.MarkQuestionUsedUseCase
import org.neteinstein.family.ui.theme.FamilyMomentsTheme
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
        every { localeProvider.currentLanguageCode() } returns "en"
        coEvery { getQuestionsUseCase(any()) } returns fakeQuestions
        coEvery { getUsedQuestionIdsUseCase() } returns emptySet()
        coEvery { markQuestionUsedUseCase(any()) } returns Unit
        return HomeViewModel(getQuestionsUseCase, localeProvider, getUsedQuestionIdsUseCase, markQuestionUsedUseCase)
    }

    @Test
    fun `switching to grid view shows every question as a card`() {
        composeTestRule.setContent {
            FamilyMomentsTheme(darkTheme = false, dynamicColor = false) {
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
            FamilyMomentsTheme(darkTheme = false, dynamicColor = false) {
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
            FamilyMomentsTheme(darkTheme = false, dynamicColor = false) {
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
