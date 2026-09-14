package org.neteinstein.couples.feature.game

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class GameUiState(
    val currentQuestion: GameQuestion? = null,
    val currentIndex: Int = 0,
    val totalQuestions: Int = 0,
    val questions: List<GameQuestion> = emptyList(),
)

class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val questions: List<GameQuestion> = GameQuestions.all.shuffled()

    init {
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
