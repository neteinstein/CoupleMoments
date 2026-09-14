package org.neteinstein.couples.feature.game

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.neteinstein.couples.domain.repository.LocaleProvider

data class GameUiState(
    val currentQuestion: GameQuestion? = null,
    val currentIndex: Int = 0,
    val totalQuestions: Int = 0,
    val questions: List<GameQuestion> = emptyList(),
)

class GameViewModel(
    private val localeProvider: LocaleProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var questions: List<GameQuestion> = emptyList()
    private var loadedLanguageCode: String? = null

    init {
        loadQuestions()
    }

    /**
     * Re-checks the OS-applied app language and reshuffles from that language's question set if
     * it changed since the last load - the user can change it via Settings > App Language without
     * this ViewModel (scoped to the Home back stack entry) being recreated, so [init] alone isn't
     * enough to pick that up. Mirrors [org.neteinstein.couples.feature.home.HomeViewModel.onScreenEntered].
     */
    fun onScreenEntered() {
        val languageCode = localeProvider.currentLanguageCode()
        if (languageCode != loadedLanguageCode) {
            loadQuestions(languageCode)
        }
    }

    private fun loadQuestions(languageCode: String = localeProvider.currentLanguageCode()) {
        loadedLanguageCode = languageCode
        questions = GameQuestions.forLanguage(languageCode).shuffled()
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
        _uiState.update {
            it.copy(
                currentQuestion = questions[prevIndex],
                currentIndex = prevIndex,
            )
        }
    }
}
