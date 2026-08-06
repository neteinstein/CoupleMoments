package org.neteinstein.family.data.repository

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UsedQuestionsRepositoryImplTest {

    private val prefs: SharedPreferences = mockk()
    private val editor: SharedPreferences.Editor = mockk(relaxed = true)
    private val context: Context = mockk()

    private lateinit var repository: UsedQuestionsRepositoryImpl

    @Before
    fun setUp() {
        every { context.getSharedPreferences(any(), Context.MODE_PRIVATE) } returns prefs
        every { prefs.edit() } returns editor
        every { editor.putStringSet(any(), any()) } returns editor
        repository = UsedQuestionsRepositoryImpl(context)
    }

    @Test
    fun `getUsedQuestionIds returns empty set when nothing stored`() = runTest {
        every { prefs.getStringSet(any(), any()) } returns emptySet()

        assertEquals(emptySet<Int>(), repository.getUsedQuestionIds())
    }

    @Test
    fun `getUsedQuestionIds parses stored ids back to ints`() = runTest {
        every { prefs.getStringSet(any(), any()) } returns setOf("1", "2", "3")

        assertEquals(setOf(1, 2, 3), repository.getUsedQuestionIds())
    }

    @Test
    fun `markAsUsed adds the id to the existing stored set`() = runTest {
        every { prefs.getStringSet(any(), any()) } returns setOf("1")

        repository.markAsUsed(2)

        verify { editor.putStringSet(any(), setOf("1", "2")) }
        verify { editor.apply() }
    }
}
