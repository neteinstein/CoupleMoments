package org.neteinstein.couples.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.neteinstein.couples.domain.model.ThemeMode
import org.neteinstein.couples.domain.repository.ThemeModeRepository

/**
 * [ThemeModeRepository] backed by a single [android.content.SharedPreferences] string - no Room
 * table needed for one setting that isn't queried, filtered, or joined against anything. The
 * [MutableStateFlow] doubles as the observable source of truth: it's seeded synchronously from
 * prefs on creation (a single [SharedPreferences] read is effectively free) and updated on every
 * write, so every collector - the Settings screen and the app-wide theme alike - sees the same
 * value without polling.
 */
class ThemeModeRepositoryImpl(
    private val context: Context,
) : ThemeModeRepository {
    private val prefs by lazy { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    private val state by lazy { MutableStateFlow(readThemeMode()) }

    override val themeMode: StateFlow<ThemeMode>
        get() = state.asStateFlow()

    override suspend fun setThemeMode(mode: ThemeMode) {
        withContext(Dispatchers.IO) {
            prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        }
        state.value = mode
    }

    private fun readThemeMode(): ThemeMode =
        prefs.getString(KEY_THEME_MODE, null)?.let { name ->
            runCatching { ThemeMode.valueOf(name) }.getOrNull()
        } ?: ThemeMode.System

    private companion object {
        const val PREFS_NAME = "theme_mode"
        const val KEY_THEME_MODE = "theme_mode"
    }
}
