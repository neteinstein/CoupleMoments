package org.neteinstein.couples.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.neteinstein.couples.domain.model.AppLanguage

/**
 * The user's explicit in-app language choice, made from the Settings screen's language picker -
 * distinct from [LocaleProvider], which only ever reflects the OS/browser's own locale. `null`
 * means "no override" - the app should fall back to [LocaleProvider] (see
 * [org.neteinstein.couples.domain.usecase.GetContentLanguageUseCase]).
 *
 * [languageOverride] is a [StateFlow] for the same reason [ThemeModeRepository.themeMode] is: the
 * app root re-reads it to re-localize the whole UI the moment the picker changes, rather than
 * waiting for whatever screen happens to be recreated next.
 *
 * On Android, changing the language is done by deep-linking into the system's per-app language
 * settings (see `feature:settings`'s language row), which changes what [LocaleProvider] itself
 * reports - this repository's override is only ever set from iOS/Web, which have no such OS-level
 * settings page to deep-link into.
 */
interface LanguagePreferenceRepository {
    val languageOverride: StateFlow<AppLanguage?>

    suspend fun setLanguageOverride(language: AppLanguage?)
}
