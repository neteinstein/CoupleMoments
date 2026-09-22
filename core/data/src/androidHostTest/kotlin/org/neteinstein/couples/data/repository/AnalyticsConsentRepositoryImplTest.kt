package org.neteinstein.couples.data.repository

import com.russhwolf.settings.Settings
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyticsConsentRepositoryImplTest {
    private val settings: Settings = mockk(relaxUnitFun = true)

    @Test
    fun `analytics is on before the user has made a choice`() =
        runTest {
            every { settings.getBoolean("analytics_enabled", true) } returns true

            assertEquals(true, AnalyticsConsentRepositoryImpl(settings).isEnabled())
        }

    @Test
    fun `a stored opt-out is honoured`() =
        runTest {
            every { settings.getBoolean("analytics_enabled", true) } returns false

            assertEquals(false, AnalyticsConsentRepositoryImpl(settings).isEnabled())
        }

    /**
     * The key is load-bearing, not incidental: on Web every named `Settings` qualifier resolves to
     * the same localStorage store, so a bare "enabled" here would be the same entry as
     * `QuestionsForParentsRepositoryImpl`'s - and `firebase-init.js` reads this exact key directly
     * to decide whether to initialize Analytics on an opted-out page load.
     */
    @Test
    fun `the opt-out is stored under the prefixed key the web bootstrap also reads`() =
        runTest {
            AnalyticsConsentRepositoryImpl(settings).setEnabled(false)

            verify { settings.putBoolean("analytics_enabled", false) }
        }
}
