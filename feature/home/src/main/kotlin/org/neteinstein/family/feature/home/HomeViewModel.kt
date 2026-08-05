package org.neteinstein.family.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.neteinstein.family.domain.model.Question
import org.neteinstein.family.domain.model.QuestionCategory
import org.neteinstein.family.domain.repository.LocaleProvider
import org.neteinstein.family.domain.usecase.GetQuestionsUseCase
import org.neteinstein.family.domain.usecase.GetUsedQuestionIdsUseCase
import org.neteinstein.family.domain.usecase.MarkQuestionUsedUseCase

data class HomeUiState(
    val currentQuestion: Question? = null,
    val isLoading: Boolean = true,
    val currentIndex: Int = 0,
    val totalQuestions: Int = 0,
    val selectedCategory: QuestionCategory? = null
)

class HomeViewModel(
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val localeProvider: LocaleProvider,
    private val getUsedQuestionIdsUseCase: GetUsedQuestionIdsUseCase,
    private val markQuestionUsedUseCase: MarkQuestionUsedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var allQuestions: List<Question> = emptyList()
    private var questions: List<Question> = emptyList()
    private var usedQuestionIds: Set<Int> = emptySet()
    private var loadedLanguageCode: String? = null

    init {
        loadQuestions()
    }

    /**
     * Re-checks the OS-applied app language and reloads questions if it changed since the last
     * load - the user can change it via Settings > App Language without this ViewModel (scoped to
     * the Home back stack entry) being recreated, so [init] alone isn't enough to pick that up.
     */
    fun onScreenEntered() {
        val languageCode = localeProvider.currentLanguageCode()
        if (languageCode != loadedLanguageCode) {
            loadQuestions(languageCode)
        }
    }

    fun loadQuestions(languageCode: String = localeProvider.currentLanguageCode()) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            loadedLanguageCode = languageCode
            allQuestions = getQuestionsUseCase(languageCode)
            usedQuestionIds = getUsedQuestionIdsUseCase()
            applyFilter(_uiState.value.selectedCategory)
        }
    }

    fun onCategorySelected(category: QuestionCategory?) {
        applyFilter(category)
    }

    fun markCurrentQuestionAsUsed() {
        val question = _uiState.value.currentQuestion ?: return
        viewModelScope.launch {
            markQuestionUsedUseCase(question.id)
            usedQuestionIds = usedQuestionIds + question.id
            applyFilter(_uiState.value.selectedCategory)
        }
    }

    private fun applyFilter(category: QuestionCategory?) {
        questions = allQuestions
            .filter { (category == null || it.category == category) && it.id !in usedQuestionIds }
            .shuffled()
        val firstQuestion = questions.firstOrNull()
        _uiState.update {
            it.copy(
                isLoading = false,
                currentQuestion = firstQuestion,
                currentIndex = 0,
                totalQuestions = questions.size,
                selectedCategory = category
            )
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
