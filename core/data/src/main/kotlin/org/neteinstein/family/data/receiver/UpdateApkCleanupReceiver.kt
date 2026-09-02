package org.neteinstein.family.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.neteinstein.family.domain.usecase.ClearDownloadedUpdateUseCase

/**
 * Deletes the APK [org.neteinstein.family.domain.usecase.DownloadAppUpdateUseCase] downloaded to
 * `context.cacheDir` once this app finishes being updated through it, so it doesn't sit there
 * wasting space indefinitely.
 *
 * Listens for [Intent.ACTION_MY_PACKAGE_REPLACED] specifically: it's one of the few broadcasts
 * exempted from Android 8+'s implicit-broadcast background restrictions (delivered only to the
 * app that was just updated, and only via a manifest-declared receiver like this one - a
 * dynamically registered one wouldn't survive the process being killed during install, which is
 * the common case), making it the only reliable signal for "the update this app itself downloaded
 * just finished installing". Deleting the file any earlier - e.g. right after firing the install
 * `Intent` in `AppUpdateInstallerImpl.installPackage` - would be unsafe, since the system Package
 * Installer may still be reading it via the `FileProvider` `content://` URI at that point.
 *
 * [KoinComponent]/[inject] rather than constructor injection: this class is instantiated by the
 * system from the manifest (see `app`'s `AndroidManifest.xml`), not by Koin itself, and Koin is
 * guaranteed already started (`FamilyMomentsApp.onCreate` always runs before any manifest
 * component can receive a broadcast in this process). [BroadcastReceiver.onReceive] isn't a
 * suspend function and has a short execution budget, so the actual cleanup runs on a
 * [goAsync]-backed coroutine instead of blocking it directly.
 */
class UpdateApkCleanupReceiver :
    BroadcastReceiver(),
    KoinComponent {
    private val clearDownloadedUpdateUseCase: ClearDownloadedUpdateUseCase by inject()

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                clearDownloadedUpdateUseCase()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
