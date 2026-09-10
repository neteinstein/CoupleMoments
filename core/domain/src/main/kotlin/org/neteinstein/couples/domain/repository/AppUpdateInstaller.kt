package org.neteinstein.couples.domain.repository

import java.io.File

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
     * Launches the system Package Installer for [apkFile]. Requires [canInstallPackages] to
     * already be true - callers are expected to check that (and route to
     * [openInstallPermissionSettings] instead) before ever calling this.
     */
    fun installPackage(apkFile: File)
}
