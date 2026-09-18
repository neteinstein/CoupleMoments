package org.neteinstein.couples.domain.usecase

import kotlinx.coroutines.flow.StateFlow
import org.neteinstein.couples.domain.model.ThemeMode
import org.neteinstein.couples.domain.repository.ThemeModeRepository

class GetThemeModeUseCase(
    private val themeModeRepository: ThemeModeRepository,
) {
    operator fun invoke(): StateFlow<ThemeMode> = themeModeRepository.themeMode
}
