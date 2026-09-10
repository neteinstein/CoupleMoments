package org.neteinstein.couples.data.repository

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IntimacyGateRepositoryImplTest {
    private val context: Context = mockk()
    private val prefs: SharedPreferences = mockk()
    private val editor: SharedPreferences.Editor = mockk()

    private lateinit var repository: IntimacyGateRepositoryImpl

    @Before
    fun setUp() {
        every { context.getSharedPreferences("intimacy_gate", Context.MODE_PRIVATE) } returns prefs
        every { prefs.edit() } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.apply() } returns Unit
        repository = IntimacyGateRepositoryImpl(context)
    }

    @Test
    fun `hasAcknowledged returns false before it's ever been set`() =
        runTest {
            every { prefs.getBoolean("acknowledged", false) } returns false

            assertFalse(repository.hasAcknowledged())
        }

    @Test
    fun `hasAcknowledged returns true once setAcknowledged has been called`() =
        runTest {
            every { prefs.getBoolean("acknowledged", false) } returns true

            assertTrue(repository.hasAcknowledged())
        }

    @Test
    fun `setAcknowledged persists the flag as true`() =
        runTest {
            repository.setAcknowledged()

            verify { editor.putBoolean("acknowledged", true) }
            verify { editor.apply() }
        }
}
