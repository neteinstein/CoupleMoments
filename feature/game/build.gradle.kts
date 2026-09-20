plugins {
    id("kmp-compose-library")
}

compose.resources {
    packageOfResClass = "org.neteinstein.couples.feature.game.resources"
}

kotlin {
    android {
        namespace = "org.neteinstein.couples.feature.game"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(project(":core:ui"))
            implementation(libs.coroutines.core)
            // koinViewModel() in the screen + the viewModel { } module builder - both multiplatform.
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.lifecycle.viewmodel)
            // Deliberately NOT lifecycle-viewmodel-compose: as of 2.10.0 it publishes Android/JVM
            // artifacts only, with no iOS or wasmJs variant, so it cannot sit in commonMain. The
            // screens get their ViewModels from Koin's koinViewModel() instead, which is
            // KMP-native.
            implementation(libs.lifecycle.runtime.compose)
        }

        androidMain.dependencies {
            // Main dispatcher runtime for viewModelScope.launch - Android target only.
            implementation(libs.coroutines.android)
        }

        androidHostTest.dependencies {
            implementation(libs.junit)
            implementation(libs.mockk)
            implementation(libs.coroutines.test)
        }
    }
}
