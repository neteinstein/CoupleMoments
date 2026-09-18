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

        androidHostTest.dependencies {
            implementation(libs.junit)
            implementation(libs.mockk)
            implementation(libs.coroutines.test)
        }
    }
}
