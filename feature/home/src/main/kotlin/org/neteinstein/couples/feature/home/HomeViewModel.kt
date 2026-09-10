package org.neteinstein.couples.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.neteinstein.couples.domain.model.Question
import org.neteinstein.couples.domain.model.QuestionCategory
import org.neteinstein.couples.domain.repository.LocaleProvider
import org.neteinstein.couples.domain.usecase.AcknowledgeIntimacyGateUseCase
import org.neteinstein.couples.domain.usecase.GetQuestionsUseCase
import org.neteinstein.couples.domain.usecase.GetUsedQuestionIdsUseCase
import org.neteinstein.couples.domain.usecase.HasAcknowledgedIntimacyGateUseCase
import org.neteinstein.couples.domain.usecase.MarkQuestionUsedUseCase

data class HomeUiState(
    val currentQuestion: Question? = null,
    val isLoading: Boolean = true,
    val currentIndex: Int = 0,
    val totalQuestions: Int = 0,
    val selectedCategory: QuestionCategory? = null,
    val questions: List<Question> = emptyList(),
    // Whether the one-time 18+ notice should be shown before switching to the Intimacy category.
    val showIntimacyGate: Boolean = false,
)

class HomeViewModel(
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val localeProvider: LocaleProvider,
    private val getUsedQuestionIdsUseCase: GetUsedQuestionIdsUseCase,
    private val markQuestionUsedUseCase: MarkQuestionUsedUseCase,
    private val hasAcknowledgedIntimacyGateUseCase: HasAcknowledgedIntimacyGateUseCase,
    private val acknowledgeIntimacyGateUseCase: AcknowledgeIntimacyGateUseCase,
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
     * Otherwise, just refreshes which cards are hidden, so returning from Settings after a
     * "Reset Cards" makes previously hidden cards reappear without needing a full reload.
     */
    fun onScreenEntered() {
        val languageCode = localeProvider.currentLanguageCode()
        if (languageCode != loadedLanguageCode) {
            loadQuestions(languageCode)
        } else {
            refreshUsedQuestions()
        }
    }

    private fun refreshUsedQuestions() {
        viewModelScope.launch {
            usedQuestionIds = getUsedQuestionIdsUseCase()
            applyFilter(_uiState.value.selectedCategory)
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

    /**
     * Routes straight to [applyFilter], except for [QuestionCategory.Intimacy]: the first time
     * it's selected, this shows the 18+ notice ([HomeUiState.showIntimacyGate]) instead and waits
     * for [confirmIntimacyGate] before actually switching - see [onIntimacyGateConfirmed] and
     * [onIntimacyGateDismissed] for how the dialog resolves that.
     */
    fun onCategorySelected(category: QuestionCategory?) {
        if (category == QuestionCategory.Intimacy) {
            viewModelScope.launch {
                if (hasAcknowledgedIntimacyGateUseCase()) {
                    applyFilter(category)
                } else {
                    _uiState.update { it.copy(showIntimacyGate = true) }
                }
            }
        } else {
            applyFilter(category)
        }
    }

    /** User confirmed the 18+ notice: persist it so it never shows again, then switch category. */
    fun onIntimacyGateConfirmed() {
        viewModelScope.launch {
            acknowledgeIntimacyGateUseCase()
            _uiState.update { it.copy(showIntimacyGate = false) }
            applyFilter(QuestionCategory.Intimacy)
        }
    }

    /** User backed out of the 18+ notice: close it without changing the selected category. */
    fun onIntimacyGateDismissed() {
        _uiState.update { it.copy(showIntimacyGate = false) }
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
        questions =
            allQuestions
                .filter { (category == null || it.category == category) && it.id !in usedQuestionIds }
                .shuffled()
        val firstQuestion = questions.firstOrNull()
        _uiState.update {
            it.copy(
                isLoading = false,
                currentQuestion = firstQuestion,
                currentIndex = 0,
                totalQuestions = questions.size,
                selectedCategory = category,
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
