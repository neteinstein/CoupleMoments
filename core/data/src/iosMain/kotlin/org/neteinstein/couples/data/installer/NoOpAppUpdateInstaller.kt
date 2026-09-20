package org.neteinstein.couples.data.installer

import org.neteinstein.couples.domain.repository.AppUpdateInstaller

/** Counterpart to [org.neteinstein.couples.data.repository.NoOpUpdateRepository] on iOS. */
class NoOpAppUpdateInstaller : AppUpdateInstaller {
    override fun canInstallPackages(): Boolean = false

    override fun openInstallPermissionSettings() = Unit

    override suspend fun installPackage(apkBytes: ByteArray) = Unit
}
