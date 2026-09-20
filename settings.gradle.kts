pluginManagement {
    // Convention plugins (`kmp-library`, `kmp-compose-library`) live in the build-logic included
    // build. Including it here, inside pluginManagement, is what lets a module apply them by bare
    // id with no version or classpath declaration.
    includeBuild("build-logic")

    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // PREFER_SETTINGS rather than FAIL_ON_PROJECT_REPOS now that the build has a wasmJs target:
    // the Kotlin Gradle plugin's Node.js/Yarn/Binaryen setup tasks register their own download
    // repositories, which FAIL_ON_PROJECT_REPOS rejects outright. Switching mode alone isn't
    // enough - the detached configurations those tasks resolve through don't fall back to a
    // project-added repository under either mode - so each distribution repository is declared
    // here directly instead, the standard workaround for this Kotlin/Wasm + centralized-
    // repository-management conflict (https://youtrack.jetbrains.com/issue/KT-52626).
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        ivy("https://nodejs.org/dist/") {
            name = "Node Distributions at https://nodejs.org/dist"
            patternLayout {
                artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
            }
            metadataSources { artifact() }
            content { includeModule("org.nodejs", "node") }
        }
        ivy("https://github.com/yarnpkg/yarn/releases/download") {
            name = "Yarn Distributions at https://github.com/yarnpkg/yarn/releases/download"
            patternLayout {
                artifact("v[revision]/[artifact](-v[revision]).[ext]")
            }
            metadataSources { artifact() }
            content { includeModule("com.yarnpkg", "yarn") }
        }
        ivy("https://github.com/WebAssembly/binaryen/releases/download") {
            name = "Binaryen Distributions at https://github.com/WebAssembly/binaryen/releases/download"
            patternLayout {
                artifact("version_[revision]/[artifact]-version_[revision]-[classifier].[ext]")
            }
            metadataSources { artifact() }
            content { includeModule("com.github.webassembly", "binaryen") }
        }
    }
}

rootProject.name = "CoupleMoments"

// Lets modules declare dependencies as `implementation(projects.core.domain)` instead of
// `project(":core:domain")`. Opt-in only - the existing `project(...)` notation keeps working, so
// modules convert as they are migrated.
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// `app` is the shared Kotlin Multiplatform aggregator (commonMain UI/navigation/DI), consumed by
// the three platform entry points below.
include(":app")
include(":androidApp")
// `iosApp` has no build.gradle.kts - it's an Xcode project wrapper only, included so IDEs and
// tooling see it as part of the project.
include(":iosApp")
include(":webApp")
include(":core:domain")
include(":core:data")
include(":core:ui")
include(":feature:splash")
include(":feature:home")
include(":feature:game")
include(":feature:settings")
