plugins {
    id("kmp-compose-library")
}

kotlin {
    android {
        namespace = "org.neteinstein.couples.feature.home"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(project(":core:ui"))
            implementation(libs.coroutines.core)
            // koinViewModel() (HomeScreen.kt) + the module-definition-side viewModel { } builder
            // (HomeModule.kt, via its transitive koin-core-viewmodel dependency) - both multiplatform.
            implementation(libs.koin.compose.viewmodel)
            // HomeViewModel extends androidx.lifecycle.ViewModel / uses viewModelScope - both
            // resolvable from commonMain since Lifecycle 2.8.0 (see gradle/libs.versions.toml).
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.runtime.compose)
        }

        androidMain.dependencies {
            // Provides the Main dispatcher's runtime implementation for viewModelScope.launch - not
            // needed at compile time by commonMain source, only for the Android target at runtime.
            implementation(libs.coroutines.android)
        }

        androidHostTest.dependencies {
            implementation(libs.junit)
            implementation(libs.mockk)
            implementation(libs.coroutines.test)
            implementation(libs.koin.test)
            implementation(libs.koin.test.junit4)
            implementation(libs.robolectric)
            implementation(libs.junit.ext)
            implementation(project.dependencies.platform(libs.compose.bom))
            implementation(libs.compose.ui.test.junit4)
            implementation(libs.compose.ui.test.manifest)
        }
    }
}

// `com.android.kotlin.multiplatform.library`'s android {} DSL has no testOptions.unitTests
// equivalent the pre-KMP com.android.library modules used for this - configuring the
// testAndroidHostTest Test task directly instead, to get full exception detail (message + cause
// chain) in CI logs rather than Gradle's default one-line "<ExceptionType> at <location>".
tasks.withType<Test>().configureEach {
    testLogging {
        events("failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStackTraces = true
        showCauses = true
    }
}
