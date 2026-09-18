import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

/**
 * Convention plugin for a Kotlin Multiplatform *library* module with no Compose UI
 * (e.g. `core:domain`). Modules that render UI use `kmp-compose-library` instead, which builds on
 * top of this one.
 *
 * Centralises what every module's build.gradle.kts repeats today: the toolchain, the Android
 * library target's compileSdk/minSdk, and the ktlint block.
 *
 * Each consuming module still has to set its own Android namespace, since that is genuinely
 * per-module:
 *
 *     kotlin {
 *         android {
 *             namespace = "org.neteinstein.couples.domain"
 *         }
 *     }
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jlleitschuh.gradle.ktlint")
}

kotlin {
    jvmToolchain(17)

    // `com.android.kotlin.multiplatform.library` contributes this `android { }` block *inside*
    // `kotlin { }` (AGP 9's KMP library DSL - earlier AGP versions called the same block
    // `androidLibrary { }`). It replaces the top-level `android { }` block the plain
    // `com.android.library` modules use today; `namespace` is set by the consuming module.
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    android {
        compileSdk = 37
        minSdk = 32

        // Host-side unit tests (src/androidHostTest + commonTest) and instrumented tests
        // respectively. NOTE: this AGP KMP-library plugin names things differently from the
        // legacy com.android.library plugin - the host-test source set is `androidHostTest` (not
        // `androidUnitTest`), and the Gradle task it registers is `testAndroidHostTest` (not
        // `testDebugUnitTest`). CI must invoke `testAndroidHostTest` explicitly, alongside the
        // still-plain-Android modules' `testDebugUnitTest`, or a KMP-converted module's tests
        // silently never run (Gradle skips a requested task name a project doesn't have, with no
        // error) - see the commit that discovered this the hard way.
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        // Required for modules that ship Android resources or Compose Multiplatform resources
        // (`compose.components.resources` packages them as Android assets).
        androidResources {
            enable = true
        }
    }

    // ---------------------------------------------------------------------------------------
    // Additional targets are added here in later rollout steps, deliberately NOT yet - the
    // migration keeps the branch Android-only until every module has been converted.
    //
    // Step 7 (iOS):
    //     listOf(
    //         iosArm64(),
    //         iosSimulatorArm64()
    //     ).forEach { iosTarget ->
    //         iosTarget.binaries.framework {
    //             baseName = <per-module framework name>
    //             isStatic = true
    //         }
    //     }
    //
    // Step 8 (Web):
    //     wasmJs { browser() }
    // ---------------------------------------------------------------------------------------
}

// Copied verbatim from the per-module build.gradle.kts files this plugin replaces, so ktlint's
// behaviour (and therefore the CI `ktlintCheck` gate) is unchanged by the migration.
ktlint {
    android.set(true)
    ignoreFailures.set(false)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}
