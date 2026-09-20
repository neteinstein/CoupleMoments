package org.neteinstein.couples.di

import org.koin.core.context.startKoin

/**
 * Starts Koin for platforms with no self-update feature to gate and no extra platform module to
 * register (iOS, Web) - called from webApp's `main()` and from iosApp's iOSApp.swift as
 * `InitKoinKt.doInitKoin()`.
 *
 * Named `doInitKoin`, not `initKoin`: Kotlin/Native's Objective-C exporter treats a top-level
 * function starting with `init` as an initializer and renames it unpredictably. androidApp calls
 * `startKoin` directly instead (see CoupleMomentsApp) since it needs to pass its own
 * `BuildConfig.UPDATES_ENABLED` and register `androidContext()`/`androidLogger()` - Kotlin/Native's
 * Objective-C framework export doesn't carry Kotlin default parameter values through to Swift, so
 * this stays genuinely zero-argument rather than defaulted.
 *
 * Deliberately returns nothing: `koin-core` (which declares `startKoin`'s `KoinApplication` return
 * type) is only an `implementation`, not `api`, dependency of `app` - returning it here would
 * require every caller module (webApp, and iosApp's generated Objective-C framework header) to
 * resolve that type too, for no benefit since nothing uses the returned instance.
 */
fun doInitKoin() {
    startKoin {
        modules(appModule(updatesEnabled = false))
    }
}
