package org.neteinstein.couples.data.repository

import com.russhwolf.settings.Settings
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class QuestionsForParentsRepositoryImplTest {
    private val settings: Settings = mockk()

    private lateinit var repository: QuestionsForParentsRepositoryImpl

    @Before
    fun setUp() {
        every { settings.putBoolean(any(), any()) } returns Unit
        repository = QuestionsForParentsRepositoryImpl(settings)
    }

    @Test
    fun `isEnabled returns false before it's ever been set`() =
        runTest {
            every { settings.getBoolean("enabled", false) } returns false

            assertFalse(repository.isEnabled())
        }

    @Test
    fun `isEnabled returns true once setEnabled(true) has been called`() =
        runTest {
            every { settings.getBoolean("enabled", false) } returns true

            assertTrue(repository.isEnabled())
        }

    @Test
    fun `setEnabled persists the flag`() =
        runTest {
            repository.setEnabled(true)

            verify { settings.putBoolean("enabled", true) }
        }

    @Test
    fun `setEnabled(false) persists false`() =
        runTest {
            repository.setEnabled(false)

            verify { settings.putBoolean("enabled", false) }
        }
}
