package org.neteinstein.family.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Exercises [parseGitHubReleaseResponse] against real GitHub "get the latest release" API
 * response shapes - needs a real desktop [org.json] implementation on the test classpath
 * (`testImplementation(libs.json)` in this module's build.gradle.kts), since android.jar's
 * org.json classes are non-functional stubs under `unitTests.isReturnDefaultValues`.
 */
class GitHubUpdateRepositoryImplTest {
    @Test
    fun `parses a valid release with an apk asset`() {
        val json =
            """
            {
              "tag_name": "v1.0.16",
              "html_url": "https://github.com/neteinstein/FamilyMoments/releases/tag/v1.0.16",
              "assets": [
                {
                  "name": "FamilyMoments-1.0.16.apk",
                  "content_type": "application/vnd.android.package-archive",
                  "browser_download_url": "https://github.com/neteinstein/FamilyMoments/releases/download/v1.0.16/FamilyMoments-1.0.16.apk"
                }
              ]
            }
            """.trimIndent()

        val update = parseGitHubReleaseResponse(json)

        assertEquals("1.0.16", update.versionName)
        assertEquals(
            "https://github.com/neteinstein/FamilyMoments/releases/download/v1.0.16/FamilyMoments-1.0.16.apk",
            update.apkDownloadUrl,
        )
    }

    @Test
    fun `strips the leading v from the tag name`() {
        val json = releaseJson(tagName = "v2.3.4", assetName = "app.apk")

        val update = parseGitHubReleaseResponse(json)

        assertEquals("2.3.4", update.versionName)
    }

    @Test
    fun `picks the apk asset even when other assets are listed first`() {
        val json =
            """
            {
              "tag_name": "v1.0.16",
              "assets": [
                { "name": "checksums.txt", "browser_download_url": "https://example.com/checksums.txt" },
                { "name": "FamilyMoments-1.0.16.apk", "browser_download_url": "https://example.com/app.apk" }
              ]
            }
            """.trimIndent()

        val update = parseGitHubReleaseResponse(json)

        assertEquals("https://example.com/app.apk", update.apkDownloadUrl)
    }

    @Test
    fun `throws when there are no assets at all`() {
        val json = """{ "tag_name": "v1.0.16" }"""

        val exception = runCatching { parseGitHubReleaseResponse(json) }.exceptionOrNull()

        assertTrue(exception is IllegalStateException)
    }

    @Test
    fun `throws when no asset ends with apk`() {
        val json = releaseJson(tagName = "v1.0.16", assetName = "release-notes.txt")

        val exception = runCatching { parseGitHubReleaseResponse(json) }.exceptionOrNull()

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
