package org.neteinstein.couples.data.repository

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.neteinstein.couples.data.local.ioDispatcher
import org.neteinstein.couples.domain.model.AppLanguage
import org.neteinstein.couples.domain.repository.LanguagePreferenceRepository

/**
 * [LanguagePreferenceRepository] backed by a single [Settings] string entry, matching the other
 * three single-value preference repositories in this package. An empty stored value means "no
 * override" - [AppLanguage.fromCode] returns null for it, which is exactly the "follow the
 * OS/browser language" signal [org.neteinstein.couples.domain.usecase.GetContentLanguageUseCase]
 * expects.
 *
 * Like [ThemeModeRepositoryImpl], the [MutableStateFlow] doubles as the observable source of
 * truth: seeded synchronously from [settings] on first access and updated on every write, so the
 * app root, Home, Game and Settings all see the same value without polling.
 */
class LanguagePreferenceRepositoryImpl(
    private val settings: Settings,
) : LanguagePreferenceRepository {
    private val state by lazy { MutableStateFlow(readLanguageOverride()) }

    override val languageOverride: StateFlow<AppLanguage?>
        get() = state.asStateFlow()

    override suspend fun setLanguageOverride(language: AppLanguage?) {
        withContext(ioDispatcher) {
            settings.putString(KEY_LANGUAGE_OVERRIDE, language?.code ?: "")
        }
        state.value = language
    }

    private fun readLanguageOverride(): AppLanguage? = settings.getStringOrNull(KEY_LANGUAGE_OVERRIDE)?.let { AppLanguage.fromCode(it) }

    private companion object {
        const val KEY_LANGUAGE_OVERRIDE = "language_override"
    }
}
