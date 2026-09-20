plugins {
    id("kmp-library")
}

kotlin {
    android {
        namespace = "org.neteinstein.couples.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.coroutines.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
        }

        androidHostTest.dependencies {
            implementation(libs.junit)
            implementation(libs.mockk)
            implementation(libs.coroutines.test)
        }
    }
}
