package org.neteinstein.couples.feature.game

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.neteinstein.couples.domain.analytics.AnalyticsEvent
import org.neteinstein.couples.domain.analytics.AnalyticsTracker
import org.neteinstein.couples.domain.usecase.GetContentLanguageUseCase

data class GameUiState(
    val currentQuestion: GameQuestion? = null,
    val currentIndex: Int = 0,
    val totalQuestions: Int = 0,
    val questions: List<GameQuestion> = emptyList(),
)

class GameViewModel(
    private val getContentLanguageUseCase: GetContentLanguageUseCase,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var questions: List<GameQuestion> = emptyList()
    private var loadedLanguageCode: String? = null

    /** Suppresses an immediate repeat only - mirrors HomeViewModel's lastReportedQuestionId. */
    private var lastReportedIndex: Int? = null

    init {
        loadQuestions()
    }

    /**
     * Re-checks the active app language ([GetContentLanguageUseCase]) and reshuffles from that
     * language's question set if it changed since the last load - the user can change it via
     * Settings > App Language without this ViewModel (scoped to the Home back stack entry) being
     * recreated, so [init] alone isn't enough to pick that up. Mirrors
     * [org.neteinstein.couples.feature.home.HomeViewModel.onScreenEntered].
     */
    fun onScreenEntered() {
        val languageCode = getContentLanguageUseCase()
        if (languageCode != loadedLanguageCode) {
            loadQuestions(languageCode)
        }
    }

    private fun loadQuestions(languageCode: String = getContentLanguageUseCase()) {
        loadedLanguageCode = languageCode
        questions = GameQuestions.forLanguage(languageCode).shuffled()
        lastReportedIndex = null
        reportQuestionViewed(0)
        _uiState.update {
            it.copy(
                currentQuestion = questions.firstOrNull(),
                currentIndex = 0,
                totalQuestions = questions.size,
                questions = questions,
            )
        }
    }

    fun nextQuestion() {
        if (questions.isEmpty()) return
        val nextIndex = (_uiState.value.currentIndex + 1) % questions.size
        reportQuestionViewed(nextIndex)
        _uiState.update {
            it.copy(
                currentQuestion = questions[nextIndex],
                currentIndex = nextIndex,
            )
        }
    }

    fun previousQuestion() {
        if (questions.isEmpty()) return
        val prevIndex = (_uiState.value.currentIndex - 1 + questions.size) % questions.size
        reportQuestionViewed(prevIndex)
        _uiState.update {
            it.copy(
                currentQuestion = questions[prevIndex],
                currentIndex = prevIndex,
            )
        }
    }

    /**
     * Game cards are positional rather than identified (see [GameQuestions]), so the deck index is
     * what identifies "the card on screen" here. Nothing about the card itself is reported - only
     * that one was viewed, and in which language.
     */
    private fun reportQuestionViewed(index: Int) {
        if (questions.isEmpty() || index == lastReportedIndex) return
        lastReportedIndex = index
        analyticsTracker.logEvent(
            AnalyticsEvent.GameQuestionViewed(languageCode = loadedLanguageCode ?: getContentLanguageUseCase()),
        )
    }
}
