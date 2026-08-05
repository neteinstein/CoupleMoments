package org.neteinstein.family.domain.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionComparatorTest {
    @Test
    fun `is newer when a later patch segment is higher`() {
        assertTrue(isNewerVersion(current = "1.0.16", candidate = "1.0.17"))
    }

    @Test
    fun `is not newer when versions are equal`() {
        assertFalse(isNewerVersion(current = "1.0.16", candidate = "1.0.16"))
    }

    @Test
    fun `is not newer when the candidate is older`() {
        assertFalse(isNewerVersion(current = "1.0.16", candidate = "1.0.9"))
    }

    @Test
    fun `compares numerically rather than lexicographically`() {
        assertTrue(isNewerVersion(current = "1.0.9", candidate = "1.0.16"))
        assertFalse(isNewerVersion(current = "1.0.16", candidate = "1.0.9"))
    }

    @Test
    fun `treats a missing trailing segment as zero`() {
        assertFalse(isNewerVersion(current = "1.0", candidate = "1.0.0"))
        assertTrue(isNewerVersion(current = "1.0", candidate = "1.0.1"))
    }

    @Test
    fun `treats a non-numeric segment as zero rather than throwing`() {
        assertFalse(isNewerVersion(current = "1.0.16", candidate = "1.0.beta"))
    }

    @Test
    fun `is newer when a leading segment increases`() {
        assertTrue(isNewerVersion(current = "1.0.99", candidate = "2.0.0"))
    }
}
