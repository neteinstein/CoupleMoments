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
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CoupleMoments"

// Lets modules declare dependencies as `implementation(projects.core.domain)` instead of
// `project(":core:domain")`. Opt-in only - the existing `project(...)` notation keeps working, so
// modules convert as they are migrated.
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")
include(":core:domain")
include(":core:data")
include(":core:ui")
include(":feature:splash")
include(":feature:home")
include(":feature:game")
include(":feature:settings")
