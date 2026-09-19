package org.neteinstein.couples.domain.usecase

import org.neteinstein.couples.domain.model.ThemeMode
import org.neteinstein.couples.domain.repository.ThemeModeRepository

class SetThemeModeUseCase(
    private val themeModeRepository: ThemeModeRepository,
) {
    suspend operator fun invoke(mode: ThemeMode) = themeModeRepository.setThemeMode(mode)
}
