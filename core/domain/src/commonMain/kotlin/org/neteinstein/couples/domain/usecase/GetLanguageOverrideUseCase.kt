package org.neteinstein.couples.domain.usecase

import kotlinx.coroutines.flow.StateFlow
import org.neteinstein.couples.domain.model.AppLanguage
import org.neteinstein.couples.domain.repository.LanguagePreferenceRepository

/**
 * The user's current in-app language override, or `null` if none is set - see
 * [GetContentLanguageUseCase]. Observable so the app root can re-localize when it changes.
 */
class GetLanguageOverrideUseCase(
    private val languagePreferenceRepository: LanguagePreferenceRepository,
) {
    operator fun invoke(): StateFlow<AppLanguage?> = languagePreferenceRepository.languageOverride
}
