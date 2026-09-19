package org.neteinstein.couples.data.capability

/**
 * Placeholder groundwork for the platform self-update capability check called out in the KMP
 * migration plan. Today, self-update availability is still driven entirely by the `github`/
 * `playstore` product flavor's `BuildConfig.UPDATES_ENABLED` (wired as a named `"updatesEnabled"`
 * Koin boolean in `app/.../di/AppModule.kt`, consumed by `feature:settings`'s
 * `SettingsViewModel`) - this function does NOT replace or touch that wiring.
 *
 * Once `core:domain` gains a real `expect fun selfUpdateSupported(): Boolean`
 * capability-check (Android `actual` returning true unconditionally since the OS-level gate is
 * the "install unknown apps" permission handled by `AppUpdateInstaller`, iOS/Web `actual`
 * returning false since neither platform can ever sideload/self-update), this function is meant
 * to become - or be replaced by - that Android `actual`. It's unconditionally `true` because, on
 * Android, self-update is *technically* always possible; whether it's *offered* in this particular
 * build remains `BuildConfig.UPDATES_ENABLED`'s job.
 *
 * Intentionally NOT wired into Koin yet - that cross-module rewiring (swapping
 * `SettingsViewModel`'s dependency from the flavor-driven boolean to the platform-capability
 * check) is a separate, later migration step once the `expect`/`actual` pair above actually
 * exists for all targets.
 */
fun selfUpdateSupportedOnAndroid(): Boolean = true
