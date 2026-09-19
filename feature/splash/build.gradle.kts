plugins {
    id("kmp-compose-library")
}

kotlin {
    android {
        namespace = "org.neteinstein.couples.feature.splash"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:ui"))
        }

        // No multiplatform equivalent declared in the catalog yet (matches core:ui's pattern) -
        // revisit once iOS/wasmJs targets exist. Not directly referenced in this module's source
        // today; kept as-is from the pre-KMP module rather than removed, to avoid an unrelated
        // cleanup during this conversion.
        androidMain.dependencies {
            implementation(libs.lifecycle.runtime.ktx)
        }
    }
}
