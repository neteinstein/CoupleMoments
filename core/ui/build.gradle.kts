plugins {
    id("kmp-compose-library")
}

// `ic_couple_moments_mark` (commonMain/composeResources/drawable) is consumed cross-module by
// feature:splash's CoupleMomentsLogo.kt - Compose Multiplatform's generated `Res` class is
// internal to its own module by default, so publicResClass is required for another module to
// reference it at all.
compose.resources {
    publicResClass = true
}

kotlin {
    android {
        namespace = "org.neteinstein.couples.ui"
    }

    sourceSets {
        // `kmp-compose-library` already adds compose.runtime/foundation/material3/ui/
        // components.resources/components.uiToolingPreview as `implementation` in commonMain -
        // see build-logic/convention/src/main/kotlin/kmp-compose-library.gradle.kts. `core:ui`
        // deliberately re-exposes the subset every downstream `feature:*` module consumes
        // transitively (they depend on `core:ui` only, not on Compose directly - see
        // feature/home/build.gradle.kts), so those need `api` here instead, exactly like this
        // module's pre-KMP `dependencies { api(...) }` block did.
        commonMain.dependencies {
            // Modifier, Rect, graphics primitives, etc. - used directly in this module's own
            // public API (e.g. OriginReveal's `modifier: Modifier`) and by every downstream
            // Composable.
            api(compose.ui)
            // Multiplatform equivalent of `androidx.compose.ui.tooling.preview` (the `@Preview`
            // annotation's home) - downstream feature modules use `@Preview` today.
            api(compose.components.uiToolingPreview)
            // MaterialTheme, ColorScheme, all Material3 widgets - required to compile
            // `CoupleMomentsTheme` itself and used throughout every feature screen.
            api(compose.material3)
            // `org.jetbrains.compose.animation:animation`, the Compose Multiplatform build of
            // `androidx.compose.animation:animation`. Required directly by this module's own
            // OriginReveal.kt (AnimatedVisibilityScope/EnterExitState/animateFloat) and re-exposed
            // because feature:home/feature:game use AnimatedVisibility/AnimatedContent themselves
            // without their own explicit Compose dependency (see feature/home/build.gradle.kts,
            // which depends only on core:domain/core:ui + non-Compose libs). NOTE: this repo's
            // network-blocked build environment could not actually resolve this accessor to
            // confirm it exists in Compose Multiplatform 1.11.1 (gradle/libs.versions.toml's
            // `composeMultiplatform` version) - it is a long-standing, well-known member of the
            // JetBrains `compose` Gradle extension (`ComposePlugin.Dependencies.animation`,
            // mirroring the AndroidX artifact split 1:1), so this is a high-confidence but
            // UNVERIFIED-IN-THIS-ENVIRONMENT choice; flag it if `:core:ui:tasks` or a real build
            // ever runs and this line is the one that fails to resolve.
            api(compose.animation)
            // `org.jetbrains.compose.material:material-icons-extended`, exposed as
            // `compose.materialIconsExtended` by the Gradle extension - the Compose Multiplatform build
            // of `androidx.compose.material:material-icons-extended`, published under the exact same
            // `androidx.compose.material.icons.*` package, so no import changes were needed at any call
            // site. Resolves the FOLLOW-UP RISK flagged below when this module was Android-only: moved
            // here (out of androidMain) now that feature:home (KMP migration step 5) uses
            // `Icons.Default.*` directly from commonMain source.
            api(compose.materialIconsExtended)
            // `compose.foundation` (Box, layout, gestures, ...) is deliberately NOT re-declared as
            // `api` here, matching the pre-KMP module, which never listed
            // `androidx.compose.foundation:foundation` explicitly either - Material3's own POM
            // depends on Foundation as `api`, so it already flows through `compose.material3`
            // above transitively. If that assumption ever turns out wrong for the Compose
            // Multiplatform artifact graph specifically, add `api(compose.foundation)` here.
        }

        // This module targets Android only for now (no iOS/wasmJs yet - see the roadmap comment
        // in kmp-library.gradle.kts), so genuinely Android-only dependencies stay scoped to
        // androidMain rather than commonMain. Each one below is a judgment call made in this step;
        // revisit when iOS/wasmJs targets are actually added.
        androidMain.dependencies {
            // Historically needed to give `compose-material-icons` (formerly declared here) an
            // explicit version - kept regardless, since some androidx.compose.* artifact resolved
            // from this classpath will eventually need it. CI caught two issues here in turn when
            // this was first added: first, omitting it entirely fails androidCompileClasspath
            // resolution with "Could not find androidx.compose.material:material-icons-extended:."
            // (empty version); then, a bare `platform(...)` call failed with "Unresolved reference
            // 'platform'" - the top-level Gradle Kotlin DSL `platform()` extension function is on
            // `DependencyHandler`, which `kotlin { sourceSets { X.dependencies { ... } } }`'s
            // `KotlinDependencyHandler` scope does not extend, so it must be qualified via
            // `project.dependencies.platform(...)` to resolve the real `DependencyHandler`.
            api(project.dependencies.platform(libs.compose.bom))
            // `lifecycle-runtime-compose`/`lifecycle-viewmodel-compose` DO ship genuine Kotlin
            // Multiplatform artifacts since Lifecycle 2.8.0 (see the versions.toml comment next to
            // `lifecycle-viewmodel`) - feature:home (KMP migration step 5) already depends on the
            // multiplatform builds directly in its own commonMain for exactly that reason. Kept
            // Android-only *here* only because nothing in core:ui itself needs them yet; if a
            // future core:ui composable needs `collectAsStateWithLifecycle`/`koinViewModel`, move
            // these two to commonMain instead of re-declaring the multiplatform artifact locally in
            // every feature module.
            api(libs.lifecycle.runtime.compose)
            api(libs.lifecycle.viewmodel.compose)
            implementation(libs.core.ktx)
            // Backs PlatformBackHandler.android.kt's actual, which wraps
            // androidx.activity.compose.BackHandler - Android-only, no multiplatform equivalent
            // declared in the catalog (see PlatformBackHandler.kt's expect/actual for why this is
            // scoped via expect/actual rather than a direct commonMain dependency).
            implementation(libs.activity.compose)

            // NOTE: the pre-KMP module also had `debugImplementation(libs.compose.ui.tooling)`
            // (the interactive Layout Inspector / live-preview tooling, as opposed to the
            // `@Preview` annotation itself). `com.android.kotlin.multiplatform.library`'s Android
            // target has no debug/release build-type source-set split the way a plain
            // `com.android.library` module does (see kmp-library.gradle.kts - only
            // androidMain/androidUnitTest/androidInstrumentedTest exist), so there is no
            // `androidDebug` source set to scope this to here. Dropped for this module; revisit
            // once `app` itself converts and needs debug-only interactive preview tooling wired
            // at the point that actually has build-type source sets.
        }
    }
}
