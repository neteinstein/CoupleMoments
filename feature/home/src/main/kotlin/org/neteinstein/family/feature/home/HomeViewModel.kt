package org.neteinstein.family.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.neteinstein.family.domain.model.Question
import org.neteinstein.family.domain.usecase.GetQuestionsUseCase

data class HomeUiState(
    val currentQuestion: Question? = null,
    val isLoading: Boolean = true,
    val currentIndex: Int = 0,
    val totalQuestions: Int = 0
)

class HomeViewModel(private val getQuestionsUseCase: GetQuestionsUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var questions: List<Question> = emptyList()

    init {
        loadQuestions()
    }

    fun loadQuestions(languageCode: String = "en") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            questions = getQuestionsUseCase(languageCode).shuffled()
            val firstQuestion = questions.firstOrNull()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    currentQuestion = firstQuestion,
                    currentIndex = 0,
                    totalQuestions = questions.size
                )
            }
        }
    }

    fun nextQuestion() {
        if (questions.isEmpty()) return
        val nextIndex = (_uiState.value.currentIndex + 1) % questions.size
        _uiState.update {
            it.copy(
                currentQuestion = questions[nextIndex],
                currentIndex = nextIndex
            )
        }
    }

    fun previousQuestion() {
        if (questions.isEmpty()) return
        val prevIndex = (_uiState.value.currentIndex - 1 + questions.size) % questions.size
        _uiState.update {
            it.copy(
                currentQuestion = questions[prevIndex],
                currentIndex = prevIndex
            )
        }
    }
}
