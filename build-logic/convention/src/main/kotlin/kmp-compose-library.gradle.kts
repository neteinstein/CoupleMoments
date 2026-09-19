/**
 * Convention plugin for a Kotlin Multiplatform library module that renders Compose Multiplatform
 * UI (`core:ui`, every `feature:*`, and the `app` glue module).
 *
 * Everything in `kmp-library` plus the Compose Multiplatform plugins and the Compose dependencies
 * that every UI module needs in commonMain. Module-specific libraries (Koin, Ktor, SQLDelight, ...)
 * stay in the module's own build.gradle.kts.
 */
plugins {
    id("kmp-library")

    // The KMP/Android-library plugins are already applied by `kmp-library` above, so re-declaring
    // them here is a no-op at apply time. They are listed anyway because Gradle only generates the
    // type-safe accessors used below (`kotlin { }`, and `compose` for `compose.runtime` & co.) for
    // plugins named in *this* script's own `plugins { }` block.
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")

    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
        }
    }
}
