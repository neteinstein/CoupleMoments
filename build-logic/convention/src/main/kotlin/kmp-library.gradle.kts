import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

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
    id("org.jetbrains.kotlinx.kover")
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
        // isIncludeAndroidResources also merges the host-test manifest, which is what supplies
        // androidx.compose.ui:ui-test-manifest's ComponentActivity to createComposeRule() under
        // Robolectric (feature:home's Compose UI tests). Without it those tests fail at
        // "Unable to resolve activity for Intent ... androidx.activity.ComponentActivity". This is
        // the KMP android-library equivalent of the classic android-library's
        // `testOptions.unitTests.isIncludeAndroidResources`.
        withHostTestBuilder {}.configure {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        // Required for modules that ship Android resources or Compose Multiplatform resources
        // (`compose.components.resources` packages them as Android assets).
        androidResources {
            enable = true
        }
    }

    // Steps 7 and 8 of the rollout: every module now targets iOS and Web alongside Android.
    //
    // Only the `app` module declares `binaries.framework { }` on top of these (see its own
    // build.gradle.kts) - it is the single framework iosApp links against, and re-invoking an
    // already-declared target there is how a consumer adds target-specific config on top of this
    // shared shape. A library module has no framework of its own; it is compiled into app's.
    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
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
    // Compose Multiplatform's resource generator (`compose.components.resources`, used by
    // `kmp-compose-library`) registers its generated accessors (e.g. `ActualResourceCollectors.kt`
    // under build/generated/compose/resourceGenerator/...) as a real Kotlin source directory, which
    // the plain `com.android.library` setup never had - so ktlint now sees and lints code this repo
    // doesn't own and can't fix. Exclude anything under a module's build/ directory; this had no
    // effect on the modules that don't generate anything there.
    filter {
        val buildDir = layout.buildDirectory.get().asFile
        exclude { it.file.startsWith(buildDir) }
    }
}
