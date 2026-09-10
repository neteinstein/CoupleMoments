package org.neteinstein.couples.data.installer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import org.neteinstein.couples.domain.repository.AppUpdateInstaller
import java.io.File

/**
 * Hands a downloaded APK (see `GitHubUpdateRepositoryImpl.downloadUpdate`) to the system Package
 * Installer, and checks/deep-links into the "install unknown apps" (sideloading) permission
 * screen that gates it - the API 26+ replacement for the old device-wide "Unknown sources"
 * toggle, granted per-app instead. `minSdk` is already 32, so no `Build.VERSION.SDK_INT` gating is
 * needed here - both platform APIs this class calls have existed since the oldest OS version this
 * app supports.
 */
class AppUpdateInstallerImpl(
    private val context: Context,
) : AppUpdateInstaller {
    override fun canInstallPackages(): Boolean = context.packageManager.canRequestPackageInstalls()

    override fun openInstallPermissionSettings() {
        val intent =
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        context.startActivity(intent)
    }

    /**
     * Uses a [FileProvider] `content://` URI rather than a plain `file://` one: a cache-dir-backed
     * file can't be shared as a raw `file://` URI with another app (the Package Installer) under
     * this app's targetSdk - that throws `FileUriExposedException` - so `app`'s manifest declares
     * a `FileProvider` whose authority matches [FILE_PROVIDER_AUTHORITY_SUFFIX] below, scoped to
     * exactly the cache subdirectory the update APK is downloaded into (see
     * `update_file_paths.xml`, `GitHubUpdateRepositoryImpl`).
     */
    override fun installPackage(apkFile: File) {
        val apkUri = FileProvider.getUriForFile(context, "${context.packageName}.$FILE_PROVIDER_AUTHORITY_SUFFIX", apkFile)
        val intent =
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        context.startActivity(intent)
    }

    private companion object {
        // Must exactly match the FileProvider <provider> authority declared in app's
        // AndroidManifest.xml (that side prefixes it with "${applicationId}.").
        const val FILE_PROVIDER_AUTHORITY_SUFFIX = "update.fileprovider"
    }
}
