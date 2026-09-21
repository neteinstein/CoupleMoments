package org.neteinstein.couples.feature.home

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.neteinstein.couples.domain.model.Question
import org.neteinstein.couples.domain.usecase.AcknowledgeIntimacyGateUseCase
import org.neteinstein.couples.domain.usecase.GetContentLanguageUseCase
import org.neteinstein.couples.domain.usecase.GetQuestionsUseCase
import org.neteinstein.couples.domain.usecase.GetUsedQuestionIdsUseCase
import org.neteinstein.couples.domain.usecase.HasAcknowledgedIntimacyGateUseCase
import org.neteinstein.couples.domain.usecase.IsQuestionsForParentsEnabledUseCase
import org.neteinstein.couples.domain.usecase.MarkQuestionUsedUseCase
import org.neteinstein.couples.ui.theme.CoupleMomentsTheme
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp")
class HomeScreenArrowKeyTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUpComposeResourcesContext() {
        initComposeResourcesTestContext()
    }

    private val fakeQuestions =
        listOf(
            Question(id = 1, text = "Arrow question one?", languageCode = "en"),
            Question(id = 2, text = "Arrow question two?", languageCode = "en"),
            Question(id = 3, text = "Arrow question three?", languageCode = "en"),
        )

    private fun buildViewModel(): HomeViewModel {
        val getQuestionsUseCase: GetQuestionsUseCase = mockk()
        val getContentLanguageUseCase: GetContentLanguageUseCase = mockk()
        val getUsedQuestionIdsUseCase: GetUsedQuestionIdsUseCase = mockk()
        val markQuestionUsedUseCase: MarkQuestionUsedUseCase = mockk()
        val hasAcknowledgedIntimacyGateUseCase: HasAcknowledgedIntimacyGateUseCase = mockk()
        val acknowledgeIntimacyGateUseCase: AcknowledgeIntimacyGateUseCase = mockk()
        val isQuestionsForParentsEnabledUseCase: IsQuestionsForParentsEnabledUseCase = mockk()
        every { getContentLanguageUseCase() } returns "en"
        coEvery { getQuestionsUseCase(any()) } returns fakeQuestions
        coEvery { getUsedQuestionIdsUseCase() } returns emptySet()
        coEvery { markQuestionUsedUseCase(any()) } returns Unit
        coEvery { hasAcknowledgedIntimacyGateUseCase() } returns true
        coEvery { acknowledgeIntimacyGateUseCase() } returns Unit
        coEvery { isQuestionsForParentsEnabledUseCase() } returns false
        return HomeViewModel(
            getQuestionsUseCase,
            getContentLanguageUseCase,
            getUsedQuestionIdsUseCase,
            markQuestionUsedUseCase,
            hasAcknowledgedIntimacyGateUseCase,
            acknowledgeIntimacyGateUseCase,
            isQuestionsForParentsEnabledUseCase,
        )
    }

    private fun setHomeContent() {
        composeTestRule.setContent {
            CoupleMomentsTheme(darkTheme = false, dynamicColor = false) {
                HomeScreen(onSettingsClick = {}, viewModel = buildViewModel())
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun press(key: Key) {
        composeTestRule.onRoot().performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `up arrow opens the current card full screen and down arrow closes it`() {
        setHomeContent()

        press(Key.DirectionUp)
        composeTestRule.onNodeWithContentDescription("Close").assertExists()

        press(Key.DirectionDown)
        composeTestRule.onNodeWithContentDescription("Close").assertDoesNotExist()
    }

    @Test
    fun `down arrow closes a card opened from the grid`() {
        setHomeContent()
        composeTestRule.onNodeWithContentDescription("Switch to grid view").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Arrow question two?").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Close").assertExists()

        press(Key.DirectionDown)

        composeTestRule.onNodeWithContentDescription("Close").assertDoesNotExist()
    }

    @Test
    fun `down arrow still closes the card after the close button was used once`() {
        setHomeContent()
        press(Key.DirectionUp)
        composeTestRule.onNodeWithContentDescription("Close").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Close").assertDoesNotExist()

        press(Key.DirectionUp)
        composeTestRule.onNodeWithContentDescription("Close").assertExists()
        press(Key.DirectionDown)

        composeTestRule.onNodeWithContentDescription("Close").assertDoesNotExist()
    }
}
