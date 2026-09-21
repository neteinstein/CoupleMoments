package org.neteinstein.couples.data.repository

import com.russhwolf.settings.Settings
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.neteinstein.couples.domain.model.AppLanguage

class LanguagePreferenceRepositoryImplTest {
    private val settings: Settings = mockk()

    @Before
    fun setUp() {
        every { settings.putString(any(), any()) } returns Unit
    }

    @Test
    fun `languageOverride is null before anything has been saved`() {
        every { settings.getStringOrNull("language_override") } returns null

        val repository = LanguagePreferenceRepositoryImpl(settings)

        assertNull(repository.languageOverride.value)
    }

    @Test
    fun `languageOverride is seeded from a previously saved value`() {
        every { settings.getStringOrNull("language_override") } returns "pt"

        val repository = LanguagePreferenceRepositoryImpl(settings)

        assertEquals(AppLanguage.PORTUGUESE, repository.languageOverride.value)
    }

    @Test
    fun `languageOverride is null for the empty 'follow the OS' value`() {
        every { settings.getStringOrNull("language_override") } returns ""

        val repository = LanguagePreferenceRepositoryImpl(settings)

        assertNull(repository.languageOverride.value)
    }

    /**
     * The observable update is what re-localizes the app root and reloads question content the
     * moment the picker changes - without it the choice only took effect on the next launch.
     */
    @Test
    fun `setLanguageOverride persists and updates the observable state`() =
        runTest {
            every { settings.getStringOrNull("language_override") } returns null
            val repository = LanguagePreferenceRepositoryImpl(settings)

            repository.setLanguageOverride(AppLanguage.GERMAN)

            verify { settings.putString("language_override", "de") }
            assertEquals(AppLanguage.GERMAN, repository.languageOverride.value)
        }

    @Test
    fun `setLanguageOverride to null clears the stored value and the observable state`() =
        runTest {
            every { settings.getStringOrNull("language_override") } returns "de"
            val repository = LanguagePreferenceRepositoryImpl(settings)

            repository.setLanguageOverride(null)

            verify { settings.putString("language_override", "") }
            assertNull(repository.languageOverride.value)
        }
}
