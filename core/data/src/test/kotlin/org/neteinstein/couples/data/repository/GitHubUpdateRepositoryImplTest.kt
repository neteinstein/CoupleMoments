package org.neteinstein.couples.data.repository

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Exercises [toAppUpdate] against real GitHub "get the latest release" API response shapes,
 * decoded via kotlinx.serialization the same way [GitHubUpdateRepositoryImpl] does (with
 * `ignoreUnknownKeys = true`, since GitHub's real response has many more fields than
 * [GitHubReleaseResponse] declares).
 */
class GitHubUpdateRepositoryImplTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parses a valid release with an apk asset`() {
        val body =
            """
            {
              "tag_name": "v1.0.16",
              "html_url": "https://github.com/neteinstein/CoupleMoments/releases/tag/v1.0.16",
              "assets": [
                {
                  "name": "CoupleMoments-1.0.16.apk",
                  "content_type": "application/vnd.android.package-archive",
                  "browser_download_url": "https://github.com/neteinstein/CoupleMoments/releases/download/v1.0.16/CoupleMoments-1.0.16.apk"
                }
              ]
            }
            """.trimIndent()

        val update = toAppUpdate(json.decodeFromString(body))

        assertEquals("1.0.16", update.versionName)
        assertEquals(
            "https://github.com/neteinstein/CoupleMoments/releases/download/v1.0.16/CoupleMoments-1.0.16.apk",
            update.apkDownloadUrl,
        )
    }

    @Test
    fun `strips the leading v from the tag name`() {
        val body = releaseJson(tagName = "v2.3.4", assetName = "app.apk")

        val update = toAppUpdate(json.decodeFromString(body))

        assertEquals("2.3.4", update.versionName)
    }

    @Test
    fun `picks the apk asset even when other assets are listed first`() {
        val body =
            """
            {
              "tag_name": "v1.0.16",
              "assets": [
                { "name": "checksums.txt", "browser_download_url": "https://example.com/checksums.txt" },
                { "name": "CoupleMoments-1.0.16.apk", "browser_download_url": "https://example.com/app.apk" }
              ]
            }
            """.trimIndent()

        val update = toAppUpdate(json.decodeFromString(body))

        assertEquals("https://example.com/app.apk", update.apkDownloadUrl)
    }

    @Test
    fun `throws when there are no assets at all`() {
        val body = """{ "tag_name": "v1.0.16" }"""

        val exception = runCatching { toAppUpdate(json.decodeFromString(body)) }.exceptionOrNull()

        assertTrue(exception is IllegalStateException)
    }

    @Test
    fun `throws when no asset ends with apk`() {
        val body = releaseJson(tagName = "v1.0.16", assetName = "release-notes.txt")

        val exception = runCatching { toAppUpdate(json.decodeFromString(body)) }.exceptionOrNull()

        assertTrue(exception is IllegalStateException)
    }

    private fun releaseJson(
        tagName: String,
        assetName: String,
    ) = """
        {
          "tag_name": "$tagName",
          "assets": [
            { "name": "$assetName", "browser_download_url": "https://example.com/$assetName" }
          ]
        }
        """.trimIndent()
}
