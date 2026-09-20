package org.neteinstein.couples.domain.usecase

import org.neteinstein.couples.domain.model.AppLanguage
import org.neteinstein.couples.domain.repository.LanguagePreferenceRepository

/** The user's current in-app language override, or `null` if none is set - see [GetContentLanguageUseCase]. */
class GetLanguageOverrideUseCase(
    private val languagePreferenceRepository: LanguagePreferenceRepository,
) {
    operator fun invoke(): AppLanguage? = languagePreferenceRepository.getLanguageOverride()
}
