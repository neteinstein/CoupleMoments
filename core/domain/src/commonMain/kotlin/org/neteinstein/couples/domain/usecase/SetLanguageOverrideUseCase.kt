package org.neteinstein.couples.domain.usecase

import org.neteinstein.couples.domain.model.AppLanguage
import org.neteinstein.couples.domain.repository.LanguagePreferenceRepository

/** Sets or clears (`null`) the user's in-app language override - see [GetContentLanguageUseCase]. */
class SetLanguageOverrideUseCase(
    private val languagePreferenceRepository: LanguagePreferenceRepository,
) {
    suspend operator fun invoke(language: AppLanguage?) = languagePreferenceRepository.setLanguageOverride(language)
}
