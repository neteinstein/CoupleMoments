package org.neteinstein.couples.domain.usecase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.neteinstein.couples.domain.model.AppLanguage
import org.neteinstein.couples.domain.repository.LanguagePreferenceRepository
import org.neteinstein.couples.domain.repository.LocaleProvider
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeLocaleProvider(
    private val languageCode: String,
) : LocaleProvider {
    override fun currentLanguageCode(): String = languageCode
}

private class FakeLanguagePreferenceRepository(
    override: AppLanguage? = null,
) : LanguagePreferenceRepository {
    private val state = MutableStateFlow(override)

    override val languageOverride: StateFlow<AppLanguage?> = state

    override suspend fun setLanguageOverride(language: AppLanguage?) {
        state.value = language
    }
}

class GetContentLanguageUseCaseTest {
    @Test
    fun `prefers the in-app override over the OS locale`() {
        val useCase =
            GetContentLanguageUseCase(
                FakeLocaleProvider("de"),
                FakeLanguagePreferenceRepository(AppLanguage.PORTUGUESE),
            )

        assertEquals("pt", useCase())
    }

    @Test
    fun `falls back to the OS locale when it is a supported language`() {
        val useCase = GetContentLanguageUseCase(FakeLocaleProvider("fr"), FakeLanguagePreferenceRepository())

        assertEquals("fr", useCase())
    }

    @Test
    fun `falls back to the default language when the OS locale is unsupported`() {
        val useCase = GetContentLanguageUseCase(FakeLocaleProvider("ja"), FakeLanguagePreferenceRepository())

        assertEquals(AppLanguage.Default.code, useCase())
    }
}
