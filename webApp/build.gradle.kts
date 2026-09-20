plugins {
    id("kmp-web-application")
}

// The Web (wasmJs) entry point - mirrors androidApp/iosApp's role for Android/iOS: a thin
// platform-specific wrapper around `app`'s shared `App()` composable. The wasmJs target,
// toolchain and ktlint setup all come from the convention plugin above.
kotlin {
    sourceSets {
        wasmJsMain.dependencies {
            implementation(project(":app"))
            // `implementation(project(":app"))` isn't transitive, so app's own Compose deps don't
            // reach this module's compile classpath - ComposeViewport needs its own dependency
            // here regardless.
            implementation(compose.ui)
        }
    }
}
