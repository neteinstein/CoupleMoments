package org.neteinstein.couples.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.neteinstein.couples.domain.model.ThemeMode

/**
 * The persisted [ThemeMode] the user picked on the Settings screen. [themeMode] is a hot,
 * always-has-a-value stream so both the Settings screen and the app-wide theme (applied at
 * [org.neteinstein.couples.domain.usecase.GetThemeModeUseCase]'s call site) observe the same
 * source of truth and update together as soon as it changes.
 */
interface ThemeModeRepository {
    val themeMode: StateFlow<ThemeMode>

    suspend fun setThemeMode(mode: ThemeMode)
}
