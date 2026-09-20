package org.neteinstein.couples.data.repository

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.neteinstein.couples.data.local.ioDispatcher
import org.neteinstein.couples.domain.model.ThemeMode
import org.neteinstein.couples.domain.repository.ThemeModeRepository

/**
 * [ThemeModeRepository] backed by a single [com.russhwolf.settings.Settings] string entry - no
 * Room table needed for one setting that isn't queried, filtered, or joined against anything. The
 * [MutableStateFlow] doubles as the observable source of truth: it's seeded synchronously from
 * [settings] on creation (a single read is effectively free) and updated on every write, so every
 * collector - the Settings screen and the app-wide theme alike - sees the same value without
 * polling.
 *
 * [settings] is expected to be bound (see `DataModule.kt`) to the exact same on-disk store the
 * previous `context.getSharedPreferences("theme_mode", MODE_PRIVATE)` used (via
 * `SharedPreferencesSettings`), so upgrading installs keep their saved theme.
 */
class ThemeModeRepositoryImpl(
    private val settings: Settings,
) : ThemeModeRepository {
    private val state by lazy { MutableStateFlow(readThemeMode()) }

    override val themeMode: StateFlow<ThemeMode>
        get() = state.asStateFlow()

    override suspend fun setThemeMode(mode: ThemeMode) {
        withContext(ioDispatcher) {
            settings.putString(KEY_THEME_MODE, mode.name)
        }
        state.value = mode
    }

    private fun readThemeMode(): ThemeMode =
        settings.getStringOrNull(KEY_THEME_MODE)?.let { name ->
            runCatching { ThemeMode.valueOf(name) }.getOrNull()
        } ?: ThemeMode.System

    private companion object {
        const val KEY_THEME_MODE = "theme_mode"
    }
}
