plugins {
    id("kmp-library")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
}

sqldelight {
    databases {
        // Generates `CoupleMomentsDatabase` (and the CardQueries/SeedMetadataQueries the two DAO
        // implementations wrap) into commonMain from src/commonMain/sqldelight/**.sq - the
        // generated code is driver-agnostic, so only DriverFactory differs per target. wasmJs has
        // no SQLDelight driver at all and never constructs the database: it binds CardDao/
        // SeedMetadataDao to storage-backed implementations instead (see WebCardDao).
        create("CoupleMomentsDatabase") {
            packageName.set("org.neteinstein.couples.data.local")
        }
    }
}

kotlin {
    android {
        namespace = "org.neteinstein.couples.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(libs.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.sqldelight.runtime)
            // The three (now four) single-value preference repositories and, on Web, the card
            // store itself all sit on this one interface - see SettingsQualifiers.kt.
            implementation(libs.multiplatform.settings)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.core.ktx)
            implementation(libs.sqldelight.android.driver)
            // Android is the only target with the GitHub-Releases self-update flow, so the Ktor
            // client and its engine are scoped here rather than to commonMain - see
            // PlatformDataModule.android.kt.
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            // Firebase Analytics (FirebaseAnalyticsTracker). The BOM pins the artifact version;
            // what actually configures the SDK at runtime is the string resources the
            // com.google.gms.google-services plugin generates in androidApp from
            // google-services.json - applied there, conditionally, since that file is gitignored.
            // `project.dependencies.platform(...)`, not a bare `platform(...)`: the KMP source-set
            // dependency DSL has no platform() of its own, unlike the Android/JVM `dependencies {}`
            // block androidApp uses.
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.analytics)
        }

        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }

        wasmJsMain.dependencies {
            // FirebaseWebAnalyticsTracker serializes each event's parameter map to a JSON string
            // for the JS bridge to JSON.parse - one crossing instead of one js() helper per arity.
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
        }

        androidHostTest.dependencies {
            implementation(libs.junit)
            implementation(libs.mockk)
            implementation(libs.coroutines.test)
            implementation(libs.koin.test)
            implementation(libs.koin.test.junit4)
            implementation(libs.robolectric)
            implementation(libs.junit.ext)
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
