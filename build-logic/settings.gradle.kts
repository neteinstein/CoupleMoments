// Standalone included build that hosts this project's Gradle convention plugins
// (build-logic/convention/src/main/kotlin/*.gradle.kts). It is wired into the main build by
// `pluginManagement { includeBuild("build-logic") }` in the root settings.gradle.kts, which is
// what makes `id("kmp-library")` / `id("kmp-compose-library")` resolvable from a module's
// `plugins { }` block without any version or repository declaration.
//
// This build deliberately has its own settings file: an included build is configured
// independently of the main build, so it needs its own repository declarations.
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        // `gradlePluginPortal()` is needed here (not just in pluginManagement) because
        // convention plugins pull the plugins they apply in as *ordinary* dependencies via their
        // plugin-marker coordinates - see convention/build.gradle.kts.
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        // Reuse the main build's catalog so plugin versions are pinned in exactly one place.
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"

include(":convention")
