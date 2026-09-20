plugins {
    id("kmp-compose-library")
}

// The shared Kotlin Multiplatform aggregator module. commonMain holds `App()` (theme + the
// install banner + navigation), the composed Koin `appModule`, MainViewModel and the navigation
// graph; androidApp/iosApp/webApp each wrap it in a few lines of platform glue.
compose.resources {
    packageOfResClass = "org.neteinstein.couples.resources"
}

kotlin {
    android {
        namespace = "org.neteinstein.couples.shared"
    }

    // Re-invokes the iOS targets `kmp-library` already declared, purely to reach their
    // `binaries.framework { }` - this is the one module that produces a framework, the single
    // binary iosApp links against. Reconfiguring an already-declared target this way (rather than
    // re-declaring it) is how a convention plugin's consumer adds target-specific config.
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "CoupleMomentsShared"
            isStatic = true
            // SQLDelight's native driver reaches SQLite through SQLiter's cinterop bindings, which
            // reference the system libsqlite3 symbols without linking it themselves. A static
            // framework doesn't carry that dependency through to the consumer, so without this the
            // iosApp link fails with "Undefined symbols: _sqlite3_bind_blob, ...". Declared here
            // rather than as an Xcode "Link Binary With Libraries" entry so the requirement travels
            // with the framework that actually creates it.
            linkerOpts("-lsqlite3")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(project(":core:data"))
            implementation(project(":core:ui"))
            implementation(project(":feature:splash"))
            implementation(project(":feature:home"))
            implementation(project(":feature:game"))
            implementation(project(":feature:settings"))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.lifecycle.runtime.compose)
            // The JetBrains-published KMP fork, not the mainline androidx.navigation artifact -
            // same androidx.navigation.* package (NavHost, composable, rememberNavController, so
            // no source changes), but the mainline one publishes no wasmJs variant at all.
            implementation(libs.navigation.compose.multiplatform)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.coroutines.android)
        }

        wasmJsMain.dependencies {
            // InstallAppBanner.wasmJs.kt persists its dismissal to localStorage through
            // multiplatform-settings' StorageSettings.
            implementation(libs.multiplatform.settings)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidHostTest.dependencies {
            implementation(libs.junit)
            implementation(libs.mockk)
            implementation(libs.coroutines.test)
        }
    }
}
