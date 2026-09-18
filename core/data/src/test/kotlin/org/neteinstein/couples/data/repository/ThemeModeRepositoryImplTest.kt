package org.neteinstein.couples.data.repository

import com.russhwolf.settings.Settings
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.neteinstein.couples.domain.model.ThemeMode

class ThemeModeRepositoryImplTest {
    private val settings: Settings = mockk()

    @Before
    fun setUp() {
        every { settings.putString(any(), any()) } returns Unit
    }

    @Test
    fun `themeMode defaults to System before anything has been saved`() {
        every { settings.getStringOrNull("theme_mode") } returns null

        val repository = ThemeModeRepositoryImpl(settings)

        assertEquals(ThemeMode.System, repository.themeMode.value)
    }

    @Test
    fun `themeMode is seeded from a previously saved value`() {
        every { settings.getStringOrNull("theme_mode") } returns ThemeMode.Dark.name

        val repository = ThemeModeRepositoryImpl(settings)

        assertEquals(ThemeMode.Dark, repository.themeMode.value)
    }

    @Test
    fun `themeMode falls back to System for an unrecognized saved value`() {
        every { settings.getStringOrNull("theme_mode") } returns "not_a_real_mode"

        val repository = ThemeModeRepositoryImpl(settings)

        assertEquals(ThemeMode.System, repository.themeMode.value)
    }

    @Test
    fun `setThemeMode persists and updates the observable state`() =
        runTest {
            every { settings.getStringOrNull("theme_mode") } returns null
            val repository = ThemeModeRepositoryImpl(settings)

            repository.setThemeMode(ThemeMode.Light)

            verify { settings.putString("theme_mode", ThemeMode.Light.name) }
            assertEquals(ThemeMode.Light, repository.themeMode.value)
        }
}
