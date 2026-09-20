import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

/**
 * Convention plugin for the wasmJs *executable* that is the Web entry point (`webApp`).
 *
 * Deliberately not built on `kmp-library`: that one declares an Android library target plus iOS and
 * wasmJs, which is the right shape for a shared module but wrong for a single-target browser
 * executable. What this shares with the others is the toolchain and the ktlint block.
 *
 * It exists as a convention plugin, rather than `webApp` simply declaring these plugins itself,
 * because build-logic is an included build in `pluginManagement`: the Kotlin/Compose plugins it
 * applies are already on the main build's classpath "with an unknown version", so a module that
 * re-requests them via `alias(libs.plugins.*)` fails plugin resolution ("already on the classpath
 * with an unknown version, so compatibility cannot be checked"), and a bare `id(...)` can't find
 * them either. Going through build-logic sidesteps both.
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jlleitschuh.gradle.ktlint")
}

kotlin {
    jvmToolchain(17)

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "couplemoments.js"
            }
        }
        binaries.executable()
    }
}

// Same reporter setup as kmp-library's, so `ktlintCheck` behaves identically for this module.
ktlint {
    android.set(true)
    ignoreFailures.set(false)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
    filter {
        val buildDir = layout.buildDirectory.get().asFile
        exclude { it.file.startsWith(buildDir) }
    }
}
