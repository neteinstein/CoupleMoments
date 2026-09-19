plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ktlint)
    // KMP migration groundwork (step 4, sub-step 2 of 3): needed for the @Serializable DTOs in
    // GitHubUpdateRepositoryImpl - the kotlinx.serialization runtime library alone isn't enough,
    // the compiler plugin generates the serializers.
    alias(libs.plugins.kotlin.serialization)
    // KMP migration groundwork (step 4, sub-step 3 of 3): replaces Room (and KSP, which had no
    // other consumer in this module) - see the local/ package doc comment for why.
    alias(libs.plugins.sqldelight)
}

sqldelight {
    databases {
        // Generates a `CoupleMomentsDatabase` class in this package, replacing the hand-written
        // Room `@Database`-annotated one of the same name - CardDaoImpl/SeedMetadataDaoImpl are
        // the only things that touch it directly (see their doc comments).
        create("CoupleMomentsDatabase") {
            packageName.set("org.neteinstein.couples.data.local")
        }
    }
}

android {
    namespace = "org.neteinstein.couples.data"
    compileSdk = 37

    defaultConfig {
        minSdk = 32
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

ktlint {
    android.set(true)
    ignoreFailures.set(false)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.coroutines.core)
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.core.ktx)
    implementation(libs.sqldelight.android.driver)
    // KMP migration groundwork (step 4, sub-step 1 of 3): ThemeModeRepositoryImpl,
    // IntimacyGateRepositoryImpl and QuestionsForParentsRepositoryImpl now depend on the
    // multiplatform `Settings` interface instead of raw `Context.getSharedPreferences(...)`, so
    // the eventual `core:data` -> `kmp-library` conversion is mechanical for these three repos.
    // This module itself is still Android-only (`com.android.library`) - only the Android actual
    // (`SharedPreferencesSettings`, wired in DataModule.kt) is used for now.
    implementation(libs.multiplatform.settings)
    // KMP migration groundwork (step 4, sub-step 2 of 3): GitHubUpdateRepositoryImpl now talks to
    // the GitHub Releases API through Ktor + kotlinx.serialization instead of HttpURLConnection +
    // org.json, so the eventual `core:data` -> `kmp-library` conversion needs no networking-layer
    // rewrite. This module itself is still Android-only (`com.android.library`) - only the OkHttp
    // engine (matching loopgain's `androidMain` wiring) is used for now.
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit4)
}
