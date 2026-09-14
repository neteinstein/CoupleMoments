package org.neteinstein.couples

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import org.neteinstein.couples.domain.model.ThemeMode
import org.neteinstein.couples.domain.usecase.GetThemeModeUseCase

/**
 * Exposes the persisted [ThemeMode] so [MainActivity] can resolve it into the light/dark colors
 * [org.neteinstein.couples.ui.theme.CoupleMomentsTheme] renders with, at the root of the compose
 * tree - shared across the whole app rather than scoped to the Settings screen.
 */
class MainViewModel(
    getThemeModeUseCase: GetThemeModeUseCase,
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = getThemeModeUseCase()
}
