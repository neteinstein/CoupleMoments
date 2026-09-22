package org.neteinstein.couples.data.repository

import com.russhwolf.settings.Settings
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsUserIdRepositoryImplTest {
    private val settings: Settings = mockk(relaxUnitFun = true)

    @Test
    fun `an existing id is reused rather than regenerated`() =
        runTest {
            every { settings.getStringOrNull("analytics_user_id") } returns "cafebabe"

            assertEquals("cafebabe", AnalyticsUserIdRepositoryImpl(settings).getOrCreate())
            verify(exactly = 0) { settings.putString(any(), any()) }
        }

    @Test
    fun `a first call mints a 128-bit hex id and persists it`() =
        runTest {
            every { settings.getStringOrNull("analytics_user_id") } returns null
            val stored = slot<String>()

            val id = AnalyticsUserIdRepositoryImpl(settings).getOrCreate()

            verify { settings.putString("analytics_user_id", capture(stored)) }
            assertEquals(id, stored.captured)
            assertEquals(32, id.length)
            assertTrue("not lowercase hex: $id", id.matches(Regex("[0-9a-f]{32}")))
        }

    /**
     * The id has to be random per install, not derived from anything the device already knows
     * about itself - that is what keeps it uncorrelatable with anything outside this app.
     */
    @Test
    fun `two fresh installs do not get the same id`() =
        runTest {
            every { settings.getStringOrNull("analytics_user_id") } returns null

            val first = AnalyticsUserIdRepositoryImpl(settings).getOrCreate()
            val second = AnalyticsUserIdRepositoryImpl(settings).getOrCreate()

            assertTrue(first != second)
        }
}
