package org.neteinstein.couples.ui

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Real user-agent strings, not invented ones - the whole point of this predicate is that it
 * matches what browsers actually send.
 */
class AndroidUserAgentTest {
    @Test
    fun `chrome on an android phone is an android device`() {
        assertTrue(
            isAndroidUserAgent(
                "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/126.0.0.0 Mobile Safari/537.36",
            ),
        )
    }

    @Test
    fun `firefox on an android phone is an android device`() {
        assertTrue(isAndroidUserAgent("Mozilla/5.0 (Android 14; Mobile; rv:127.0) Gecko/127.0 Firefox/127.0"))
    }

    @Test
    fun `samsung internet on an android phone is an android device`() {
        assertTrue(
            isAndroidUserAgent(
                "Mozilla/5.0 (Linux; Android 13; SAMSUNG SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "SamsungBrowser/23.0 Chrome/115.0.0.0 Mobile Safari/537.36",
            ),
        )
    }

    @Test
    fun `an android tablet is an android device`() {
        // Tablets omit "Mobile" where phones include it; the banner covers both, since the Play
        // Store listing installs on either.
        assertTrue(
            isAndroidUserAgent(
                "Mozilla/5.0 (Linux; Android 13; SM-X710) AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/126.0.0.0 Safari/537.36",
            ),
        )
    }

    @Test
    fun `desktop chrome is not an android device`() {
        assertFalse(
            isAndroidUserAgent(
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/126.0.0.0 Safari/537.36",
            ),
        )
    }

    @Test
    fun `an iphone is not an android device`() {
        // Safari on iOS says "Mobile" too, so matching on that alone would wrongly show the banner.
        assertFalse(
            isAndroidUserAgent(
                "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) " +
                    "Version/17.5 Mobile/15E148 Safari/604.1",
            ),
        )
    }

    @Test
    fun `an ipad is not an android device`() {
        assertFalse(
            isAndroidUserAgent(
                "Mozilla/5.0 (iPad; CPU OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) " +
                    "Version/17.5 Mobile/15E148 Safari/604.1",
            ),
        )
    }

    @Test
    fun `an empty user agent is not an android device`() {
        assertFalse(isAndroidUserAgent(""))
    }
}
