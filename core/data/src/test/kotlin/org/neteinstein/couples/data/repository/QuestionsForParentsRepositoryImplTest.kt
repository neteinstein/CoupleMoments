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

class QuestionsForParentsRepositoryImplTest {
    private val context: Context = mockk()
    private val prefs: SharedPreferences = mockk()
    private val editor: SharedPreferences.Editor = mockk()

    private lateinit var repository: QuestionsForParentsRepositoryImpl

    @Before
    fun setUp() {
        every { context.getSharedPreferences("questions_for_parents", Context.MODE_PRIVATE) } returns prefs
        every { prefs.edit() } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.apply() } returns Unit
        repository = QuestionsForParentsRepositoryImpl(context)
    }

    @Test
    fun `isEnabled returns false before it's ever been set`() =
        runTest {
            every { prefs.getBoolean("enabled", false) } returns false

            assertFalse(repository.isEnabled())
        }

    @Test
    fun `isEnabled returns true once setEnabled(true) has been called`() =
        runTest {
            every { prefs.getBoolean("enabled", false) } returns true

            assertTrue(repository.isEnabled())
        }

    @Test
    fun `setEnabled persists the flag`() =
        runTest {
            repository.setEnabled(true)

            verify { editor.putBoolean("enabled", true) }
            verify { editor.apply() }
        }

    @Test
    fun `setEnabled(false) persists false`() =
        runTest {
            repository.setEnabled(false)

            verify { editor.putBoolean("enabled", false) }
            verify { editor.apply() }
        }
}
