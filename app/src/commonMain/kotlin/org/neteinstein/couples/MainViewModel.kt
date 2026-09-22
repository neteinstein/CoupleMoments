package org.neteinstein.couples

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.neteinstein.couples.domain.model.AppLanguage
import org.neteinstein.couples.domain.model.ThemeMode
import org.neteinstein.couples.domain.usecase.GetLanguageOverrideUseCase
import org.neteinstein.couples.domain.usecase.GetThemeModeUseCase
import org.neteinstein.couples.domain.usecase.InitializeAnalyticsUseCase

/**
 * Exposes the two app-wide preferences the root of the compose tree needs: the persisted
 * [ThemeMode] that [org.neteinstein.couples.ui.theme.CoupleMomentsTheme] resolves into light/dark
 * colors, and the in-app [languageOverride] that [App] applies to the UI strings - both shared
 * across the whole app rather than scoped to the Settings screen.
 *
 * Also the app's one analytics bootstrap point ([InitializeAnalyticsUseCase]): this ViewModel is
 * created once per process, on every platform, before any screen composes, which is exactly the
 * lifecycle the SDK's consent/user-id/user-property setup needs. Doing it here rather than in each
 * platform entry point keeps it to a single commonMain call site.
 */
class MainViewModel(
    getThemeModeUseCase: GetThemeModeUseCase,
    getLanguageOverrideUseCase: GetLanguageOverrideUseCase,
    private val initializeAnalyticsUseCase: InitializeAnalyticsUseCase,
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = getThemeModeUseCase()
    val languageOverride: StateFlow<AppLanguage?> = getLanguageOverrideUseCase()

    init {
        viewModelScope.launch { initializeAnalyticsUseCase() }
    }
}
