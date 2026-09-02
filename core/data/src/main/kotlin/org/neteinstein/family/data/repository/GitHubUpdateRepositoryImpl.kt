package org.neteinstein.family.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.neteinstein.family.domain.model.AppUpdate
import org.neteinstein.family.domain.model.UpdateCheckResult
import org.neteinstein.family.domain.repository.UpdateRepository
import org.neteinstein.family.domain.util.isNewerVersion
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * [UpdateRepository] backed by the public GitHub Releases REST API for this project's own repo
 * (`neteinstein/FamilyMoments`) - see `.github/workflows/release.yml` for how each release and its
 * APK asset are produced. Uses a plain [HttpURLConnection] + Android's built-in [org.json] - no
 * additional networking dependency.
 *
 * The endpoint is unauthenticated (no API key needed to read public release metadata), but GitHub
 * 403s any request with no `User-Agent` header, so [fetchLatestReleaseJson] always sets one.
 */
class GitHubUpdateRepositoryImpl(
    private val context: Context,
) : UpdateRepository {
    override suspend fun checkForUpdate(): Result<UpdateCheckResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val update = parseGitHubReleaseResponse(fetchLatestReleaseJson())
                val currentVersionName = currentVersionName()
                if (isNewerVersion(current = currentVersionName, candidate = update.versionName)) {
                    UpdateCheckResult.UpdateAvailable(update)
                } else {
                    UpdateCheckResult.UpToDate(currentVersionName)
                }
            }
        }

    override suspend fun downloadUpdate(update: AppUpdate): Result<File> =
        withContext(Dispatchers.IO) {
            runCatching {
                val updatesDir = updatesDir()
                // Only one downloaded update is ever "current" - clear out anything left over
                // from a previous check before writing the new one.
                updatesDir.deleteRecursively()
                updatesDir.mkdirs()

                val apkFile = File(updatesDir, "FamilyMoments-${update.versionName}.apk")
                downloadToFile(url = update.apkDownloadUrl, destination = apkFile)
                apkFile
            }
        }

    override suspend fun clearDownloadedUpdate(): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                updatesDir().deleteRecursively()
                Unit
            }
        }

    private fun updatesDir(): File = File(context.cacheDir, UPDATE_CACHE_DIR_NAME)

    // getPackageInfo(String, Int) is deprecated in favor of the PackageInfoFlags overload added in
    // API 33, but minSdk is 32 - there's no non-deprecated way to read this below API 33.
    @Suppress("DEPRECATION")
    private fun currentVersionName(): String =
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
            ?: error("Installed package has no versionName")

    private fun fetchLatestReleaseJson(): String {
        val connection = URL(LATEST_RELEASE_URL).openConnection() as HttpURLConnection
        try {
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("User-Agent", "FamilyMoments-Android")

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                val errorBody =
                    connection.errorStream
                        ?.bufferedReader(Charsets.UTF_8)
                        ?.readText()
                        .orEmpty()
                error("GitHub API error $responseCode: $errorBody")
            }

            return connection.inputStream.bufferedReader(Charsets.UTF_8).readText()
        } finally {
            connection.disconnect()
        }
    }

    private fun downloadToFile(
        url: String,
        destination: File,
    ) {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                error("APK download failed with HTTP $responseCode")
            }
            connection.inputStream.use { input ->
                destination.outputStream().use { output -> input.copyTo(output) }
            }
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val LATEST_RELEASE_URL = "https://api.github.com/repos/neteinstein/FamilyMoments/releases/latest"
        const val UPDATE_CACHE_DIR_NAME = "updates"
    }
}

/**
 * Parses a GitHub "get the latest release" API response
 * (https://docs.github.com/en/rest/releases/releases#get-the-latest-release) into an [AppUpdate].
 * Kept as a standalone top-level function (rather than a private method) so it's directly
 * unit-testable without a fake HTTP layer.
 *
 * [AppUpdate.versionName] strips the tag's leading "v" (this repo's release tags are always
 * "v<versionName>" - see `.github/workflows/release.yml`) to match `PackageManager`'s own
 * versionName format exactly, so [isNewerVersion] compares like-for-like. The APK asset is
 * identified by filename suffix (`.apk`) rather than by position, since the release's `assets`
 * array could in principle list other files first.
 */
internal fun parseGitHubReleaseResponse(json: String): AppUpdate {
    val root = JSONObject(json)
    val versionName = root.getString("tag_name").removePrefix("v")
    val assets = root.optJSONArray("assets") ?: JSONArray()
    val apkAsset =
        (0 until assets.length())
            .map { index -> assets.getJSONObject(index) }
            .firstOrNull { asset -> asset.optString("name").endsWith(".apk", ignoreCase = true) }
            ?: error("Latest release ($versionName) has no APK attached")

    return AppUpdate(versionName = versionName, apkDownloadUrl = apkAsset.getString("browser_download_url"))
}
