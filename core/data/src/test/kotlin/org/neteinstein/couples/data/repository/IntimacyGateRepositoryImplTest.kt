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

class IntimacyGateRepositoryImplTest {
    private val settings: Settings = mockk()

    private lateinit var repository: IntimacyGateRepositoryImpl

    @Before
    fun setUp() {
        every { settings.putBoolean(any(), any()) } returns Unit
        repository = IntimacyGateRepositoryImpl(settings)
    }

    @Test
    fun `hasAcknowledged returns false before it's ever been set`() =
        runTest {
            every { settings.getBoolean("acknowledged", false) } returns false

            assertFalse(repository.hasAcknowledged())
        }

    @Test
    fun `hasAcknowledged returns true once setAcknowledged has been called`() =
        runTest {
            every { settings.getBoolean("acknowledged", false) } returns true

            assertTrue(repository.hasAcknowledged())
        }

    @Test
    fun `setAcknowledged persists the flag as true`() =
        runTest {
            repository.setAcknowledged()

            verify { settings.putBoolean("acknowledged", true) }
        }
}
