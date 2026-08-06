package org.neteinstein.family.feature.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.neteinstein.family.domain.model.Question
import org.neteinstein.family.domain.model.QuestionCategory
import org.neteinstein.family.domain.repository.LocaleProvider
import org.neteinstein.family.domain.usecase.GetQuestionsUseCase
import org.neteinstein.family.domain.usecase.GetUsedQuestionIdsUseCase
import org.neteinstein.family.domain.usecase.MarkQuestionUsedUseCase
import org.neteinstein.family.ui.theme.FamilyMomentsTheme
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Regression coverage for the Home category filter dropdown, which previously crashed when
 * opened after [QuestionCategory] labels were switched from a plain property to a `@Composable`
 * lookup resolved through `selectedCategory?.displayLabel() ?: stringResource(...)`. That
 * elvis/safe-call combination is fragile for composable calls, so [CategoryDropdown] now resolves
 * the label with an explicit if/else instead - these tests render the real dropdown (not just the
 * ViewModel) so a regression there fails here, not just in manual testing.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HomeScreenCategoryDropdownTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val fakeQuestions = listOf(
        Question(id = 1, text = "Question 1?", languageCode = "en", category = QuestionCategory.Memories),
        Question(id = 2, text = "Question 2?", languageCode = "en", category = QuestionCategory.Values)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

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
    fun `pressing the category filter opens the dropdown without crashing and lists every category`() {
        composeTestRule.setContent {
            FamilyMomentsTheme(darkTheme = false, dynamicColor = false) {
                HomeScreen(onSettingsClick = {}, viewModel = buildViewModel())
            }
        }

        composeTestRule.onNodeWithContentDescription("Filter by category").performClick()

        QuestionCategory.all.forEach { category ->
            composeTestRule.onNodeWithText("${category.emoji} ${category.expectedName()}").assertExists()
        }
    }

    @Test
    fun `selecting a category from the dropdown closes the menu and updates the selection`() {
        composeTestRule.setContent {
            FamilyMomentsTheme(darkTheme = false, dynamicColor = false) {
                HomeScreen(onSettingsClick = {}, viewModel = buildViewModel())
            }
        }

        composeTestRule.onNodeWithContentDescription("Filter by category").performClick()
        composeTestRule.onNodeWithText("❤️ Values").performClick()

        composeTestRule.onNodeWithText("❤️ Values").assertExists()
        composeTestRule.onNodeWithText("🎉 Ice Breakers").assertDoesNotExist()
    }

    private fun QuestionCategory.expectedName(): String = when (this) {
        QuestionCategory.IceBreakers -> "Ice Breakers"
        QuestionCategory.Memories -> "Memories"
        QuestionCategory.Values -> "Values"
        QuestionCategory.FutureDreams -> "Future Dreams"
        QuestionCategory.DailyLife -> "Daily Life"
    }
}
