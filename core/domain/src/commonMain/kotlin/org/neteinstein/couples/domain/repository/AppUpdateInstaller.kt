package org.neteinstein.couples.domain.repository

/**
 * Hands a downloaded update APK to the system Package Installer, and checks/deep-links into the
 * "install unknown apps" (sideloading) permission screen that gates it. Declared here (rather than
 * directly as an Android-`Context`-backed class in `feature:settings`) so the feature module can
 * keep depending only on `core:domain`/`core:ui`, per this project's module boundary rules - the
 * real implementation lives in `core:data`.
 */
interface AppUpdateInstaller {
    /** True once the user has allowed the app to install packages from outside the Play Store. */
    fun canInstallPackages(): Boolean

    /** Deep-links into this app's own "install unknown apps" toggle in system Settings. */
    fun openInstallPermissionSettings()

    /**
     * Writes [apkBytes] to wherever this platform's implementation needs them staged (e.g. a
     * FileProvider-scoped cache directory on Android) and launches the system Package Installer.
     * Takes raw bytes rather than a path so callers never need platform file-path knowledge -
     * only the implementation, which owns where it stages the file, does. Requires
     * [canInstallPackages] to already be true - callers are expected to check that (and route to
     * [openInstallPermissionSettings] instead) before ever calling this.
     */
    suspend fun installPackage(apkBytes: ByteArray)
}
