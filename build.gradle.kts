// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.play.publisher) apply false
    // Used by core:data (KMP migration step 4, sub-steps 2 and 3) - see its build.gradle.kts.
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.sqldelight) apply false
    // Applied for real, unlike the plugins above: this root project is the Kover "merging module"
    // that aggregates coverage from every KMP module below, via kover(project(...)) dependencies.
    alias(libs.plugins.kover)
}

kover {
    reports {
        total {
            xml {
                onCheck = false
            }

            // Without these the headline number is meaningless: QuestionSeedData alone is ~6,500
            // lines of hardcoded question content (data, not logic - the repository that reads it
            // is covered instead), and the rest are machine-generated files no test should be
            // written against.
            filters {
                excludes {
                    classes(
                        "org.neteinstein.couples.data.source.QuestionSeedData*",
                        // SQLDelight's generated database/query types.
                        "org.neteinstein.couples.data.local.CoupleMomentsDatabase*",
                        "org.neteinstein.couples.data.local.CardQueries*",
                        "org.neteinstein.couples.data.local.SeedMetadataQueries*",
                        // Compose Multiplatform's generated resource accessors (Res.string.*).
                        "*.resources.Res*",
                        "*ComposableSingletons*",
                    )
                }
            }
        }
    }
}

dependencies {
    kover(project(":core:domain"))
    kover(project(":core:data"))
    kover(project(":core:ui"))
    kover(project(":feature:splash"))
    kover(project(":feature:home"))
    kover(project(":feature:game"))
    kover(project(":feature:settings"))
    kover(project(":app"))
}

// No manually-registered root `clean` task (Android Studio's usual boilerplate) any more: now
// that modules target wasmJs, the Kotlin Gradle plugin's Node.js/Yarn tooling applies the `base`
// plugin - and its own `clean` task - to the *root* project, and registering a second task with
// that name fails the build outright ("Cannot add task 'clean' as a task with that name already
// exists"). Root cleaning is left to the task that plugin now provides, which does the same thing.
