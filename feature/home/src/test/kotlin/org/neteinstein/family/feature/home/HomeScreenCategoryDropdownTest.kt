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
import org.neteinstein.family.domain.repository.LocaleProvider
import org.neteinstein.family.domain.usecase.GetQuestionsUseCase
import org.neteinstein.family.domain.usecase.GetUsedQuestionIdsUseCase
import org.neteinstein.family.domain.usecase.MarkQuestionUsedUseCase
import org.neteinstein.family.ui.theme.FamilyMomentsTheme
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Regression coverage for the Home category filter dropdown, which previously crashed when
 * opened after [org.neteinstein.family.domain.model.QuestionCategory] labels were switched from a
 * plain property to a `@Composable` lookup. These tests render the real dropdown (not just the
 * ViewModel) so a regression there fails here, not just in manual testing.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp")
class HomeScreenCategoryDropdownTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun buildViewModel(): HomeViewModel {
        val getQuestionsUseCase: GetQuestionsUseCase = mockk()
        val localeProvider: LocaleProvider = mockk()
        val getUsedQuestionIdsUseCase: GetUsedQuestionIdsUseCase = mockk()
        val markQuestionUsedUseCase: MarkQuestionUsedUseCase = mockk()
        every { localeProvider.currentLanguageCode() } returns "en"
        // No fake questions: with a current question on screen, its own CategoryPill would show
        // the same "<emoji> <name>" text as a dropdown item of the same category, and
        // onNodeWithText requires exactly one match across the whole semantics tree (the popup
        // doesn't hide nodes behind it). An empty list keeps the card's own category text off
        // screen entirely, so it can never collide with the one in the dropdown.
        coEvery { getQuestionsUseCase(any()) } returns emptyList()
        coEvery { getUsedQuestionIdsUseCase() } returns emptySet()
        coEvery { markQuestionUsedUseCase(any()) } returns Unit
        return HomeViewModel(getQuestionsUseCase, localeProvider, getUsedQuestionIdsUseCase, markQuestionUsedUseCase)
    }

    @Test
    fun `pressing the category filter opens the dropdown without crashing and lists every category`() {
        composeTestRule.setContent {
            FamilyMomentsTheme(darkTheme = false, dynamicColor = false) {
                HomeScreen(onSettingsClick = {}, viewModel = buildViewModel())
            }
        }

        composeTestRule.onNodeWithContentDescription("Filter by category").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("🎉 Ice Breakers").assertExists()
        composeTestRule.onNodeWithText("📸 Memories").assertExists()
        composeTestRule.onNodeWithText("❤️ Values").assertExists()
        composeTestRule.onNodeWithText("🔮 Future Dreams").assertExists()
        composeTestRule.onNodeWithText("🌻 Daily Life").assertExists()
    }

    @Test
    fun `selecting a category from the dropdown closes the menu and updates the selection`() {
        composeTestRule.setContent {
            FamilyMomentsTheme(darkTheme = false, dynamicColor = false) {
                HomeScreen(onSettingsClick = {}, viewModel = buildViewModel())
            }
        }

        composeTestRule.onNodeWithContentDescription("Filter by category").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("❤️ Values").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("❤️ Values").assertExists()
        composeTestRule.onNodeWithText("🎉 Ice Breakers").assertDoesNotExist()
    }
}
