package org.neteinstein.couples

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import org.neteinstein.couples.domain.model.AppLanguage
import org.neteinstein.couples.domain.model.ThemeMode
import org.neteinstein.couples.domain.usecase.GetLanguageOverrideUseCase
import org.neteinstein.couples.domain.usecase.GetThemeModeUseCase

/**
 * Exposes the two app-wide preferences the root of the compose tree needs: the persisted
 * [ThemeMode] that [org.neteinstein.couples.ui.theme.CoupleMomentsTheme] resolves into light/dark
 * colors, and the in-app [languageOverride] that [App] applies to the UI strings - both shared
 * across the whole app rather than scoped to the Settings screen.
 */
class MainViewModel(
    getThemeModeUseCase: GetThemeModeUseCase,
    getLanguageOverrideUseCase: GetLanguageOverrideUseCase,
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = getThemeModeUseCase()
    val languageOverride: StateFlow<AppLanguage?> = getLanguageOverrideUseCase()
}
