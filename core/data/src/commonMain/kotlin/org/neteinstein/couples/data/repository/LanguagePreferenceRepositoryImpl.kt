package org.neteinstein.couples.data.repository

import com.russhwolf.settings.Settings
import org.neteinstein.couples.domain.model.AppLanguage
import org.neteinstein.couples.domain.repository.LanguagePreferenceRepository

/**
 * [LanguagePreferenceRepository] backed by a single [Settings] string entry, matching the other
 * three single-value preference repositories in this package. An empty stored value means "no
 * override" - [AppLanguage.fromCode] returns null for it, which is exactly the "follow the
 * OS/browser language" signal [org.neteinstein.couples.domain.usecase.GetContentLanguageUseCase]
 * expects.
 */
class LanguagePreferenceRepositoryImpl(
    private val settings: Settings,
) : LanguagePreferenceRepository {
    override fun getLanguageOverride(): AppLanguage? = settings.getStringOrNull(KEY_LANGUAGE_OVERRIDE)?.let { AppLanguage.fromCode(it) }

    override suspend fun setLanguageOverride(language: AppLanguage?) {
        settings.putString(KEY_LANGUAGE_OVERRIDE, language?.code ?: "")
    }

    private companion object {
        const val KEY_LANGUAGE_OVERRIDE = "language_override"
    }
}
