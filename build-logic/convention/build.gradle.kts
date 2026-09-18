import org.gradle.plugin.use.PluginDependency

plugins {
    `kotlin-dsl`
}

group = "org.neteinstein.couples.buildlogic"

// NOTE: deliberately no `jvmToolchain(...)` here. build-logic's output is loaded by the Gradle
// daemon itself, so compiling it with whatever JDK runs Gradle is correct - and pinning a
// toolchain would make build-logic (and therefore the whole build) unbuildable on a machine that
// has, say, only JDK 21 installed. The `jvmToolchain(17)` that matters is the one the convention
// plugins apply to the modules they configure.

// The convention plugins in src/main/kotlin apply other plugins by id, so those plugins'
// implementations have to be on this project's compile classpath. A version catalog *plugin* alias
// is not a dependency coordinate, so map each alias onto its Gradle plugin-marker artifact
// ("<plugin id>:<plugin id>.gradle.plugin:<version>"), which is what the `plugins { }` block
// resolves under the hood. Versions therefore still come from gradle/libs.versions.toml only.
fun pluginMarker(plugin: Provider<PluginDependency>): String =
    plugin.get().let { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }

dependencies {
    implementation(pluginMarker(libs.plugins.kotlin.multiplatform))
    implementation(pluginMarker(libs.plugins.android.kotlin.multiplatform.library))
    implementation(pluginMarker(libs.plugins.compose.multiplatform))
    implementation(pluginMarker(libs.plugins.kotlin.compose))
    implementation(pluginMarker(libs.plugins.ktlint))
}

// NOTE: there is intentionally no `gradlePlugin { plugins { register(...) } }` block here.
// `kmp-library` and `kmp-compose-library` are *precompiled script plugins*
// (src/main/kotlin/<plugin id>.gradle.kts); the `kotlin-dsl` plugin registers each of them
// automatically under an id derived from the file name, and declaring the same id a second time
// here would be a duplicate-registration conflict. Explicit `gradlePlugin { }` entries are the
// pattern for class-based `Plugin<Project>` implementations instead - if this build ever needs
// those (e.g. logic that is awkward to express in a script), register them there and keep the
// script plugins auto-registered.
