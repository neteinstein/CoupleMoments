package org.neteinstein.couples.data.repository

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.neteinstein.couples.domain.model.AppUpdate
import org.neteinstein.couples.domain.model.UpdateCheckResult
import org.neteinstein.couples.domain.repository.UpdateRepository
import org.neteinstein.couples.domain.util.isNewerVersion
import java.io.File

/**
 * [UpdateRepository] backed by the public GitHub Releases REST API for this project's own repo
 * (`neteinstein/CoupleMoments`) - see `.github/workflows/release.yml` for how each release and its
 * APK asset are produced. Uses Ktor's [HttpClient] (with the kotlinx.serialization JSON content
 * negotiation plugin installed - see `DataModule.kt`) for both the release-metadata request and
 * the APK download.
 *
 * The endpoint is unauthenticated (no API key needed to read public release metadata), but GitHub
 * 403s any request with no `User-Agent` header, so [fetchLatestRelease] always sets one.
 */
class GitHubUpdateRepositoryImpl(
    private val context: Context,
    private val httpClient: HttpClient,
) : UpdateRepository {
    override suspend fun checkForUpdate(): Result<UpdateCheckResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val update = toAppUpdate(fetchLatestRelease())
                val currentVersionName = currentVersionName()
                if (isNewerVersion(current = currentVersionName, candidate = update.versionName)) {
                    UpdateCheckResult.UpdateAvailable(update)
                } else {
                    UpdateCheckResult.UpToDate(currentVersionName)
                }
            }
        }

    override suspend fun downloadUpdate(update: AppUpdate): Result<ByteArray> =
        withContext(Dispatchers.IO) {
            runCatching { downloadBytes(update.apkDownloadUrl) }
        }

    override suspend fun clearDownloadedUpdate(): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                updatesDir().deleteRecursively()
                Unit
            }
        }

    // Not written to by this class any more (downloadUpdate fetches bytes directly - see below) -
    // kept only so clearDownloadedUpdate() can clear whatever AppUpdateInstallerImpl staged there
    // during a previous install. Must match update_file_paths.xml's <cache-path path="updates/">
    // and AppUpdateInstallerImpl's own copy of this constant.
    private fun updatesDir(): File = File(context.cacheDir, UPDATE_CACHE_DIR_NAME)

    // getPackageInfo(String, Int) is deprecated in favor of the PackageInfoFlags overload added in
    // API 33, but minSdk is 32 - there's no non-deprecated way to read this below API 33.
    @Suppress("DEPRECATION")
    private fun currentVersionName(): String =
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
            ?: error("Installed package has no versionName")

    private suspend fun fetchLatestRelease(): GitHubReleaseResponse {
        val response =
            httpClient.get(LATEST_RELEASE_URL) {
                header(HttpHeaders.Accept, "application/vnd.github+json")
                header(HttpHeaders.UserAgent, "CoupleMoments-Android")
            }

        if (response.status != HttpStatusCode.OK) {
            val errorBody = response.bodyAsText()
            error("GitHub API error ${response.status.value}: $errorBody")
        }

        return response.body()
    }

    private suspend fun downloadBytes(url: String): ByteArray {
        val response = httpClient.get(url)
        if (response.status != HttpStatusCode.OK) {
            error("APK download failed with HTTP ${response.status.value}")
        }
        return response.body()
    }

    private companion object {
        const val LATEST_RELEASE_URL = "https://api.github.com/repos/neteinstein/CoupleMoments/releases/latest"
        const val UPDATE_CACHE_DIR_NAME = "updates"
    }
}

/**
 * Partial mirror of a GitHub "get the latest release" API response
 * (https://docs.github.com/en/rest/releases/releases#get-the-latest-release) - only the fields
 * this app actually reads are declared. The real response has many more fields, so the [Json][
 * kotlinx.serialization.json.Json] instance installed via `ContentNegotiation` in `DataModule.kt`
 * must set `ignoreUnknownKeys = true`.
 */
@Serializable
internal data class GitHubReleaseResponse(
    @SerialName("tag_name") val tagName: String,
    val assets: List<GitHubReleaseAsset> = emptyList(),
)

@Serializable
internal data class GitHubReleaseAsset(
    val name: String,
    @SerialName("browser_download_url") val browserDownloadUrl: String,
)

/**
 * Maps a parsed [GitHubReleaseResponse] into an [AppUpdate]. Kept as a standalone top-level
 * function (rather than a private method) so it's directly unit-testable without a fake HTTP
 * layer.
 *
 * [AppUpdate.versionName] strips the tag's leading "v" (this repo's release tags are always
 * "v<versionName>" - see `.github/workflows/release.yml`) to match `PackageManager`'s own
 * versionName format exactly, so [isNewerVersion] compares like-for-like. The APK asset is
 * identified by filename suffix (`.apk`) rather than by position, since the release's `assets`
 * array could in principle list other files first.
 */
internal fun toAppUpdate(release: GitHubReleaseResponse): AppUpdate {
    val versionName = release.tagName.removePrefix("v")
    val apkAsset =
        release.assets
            .firstOrNull { asset -> asset.name.endsWith(".apk", ignoreCase = true) }
            ?: error("Latest release ($versionName) has no APK attached")

    return AppUpdate(versionName = versionName, apkDownloadUrl = apkAsset.browserDownloadUrl)
}
