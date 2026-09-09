# R8 configuration for the minified/obfuscated `release` build type (see the `release` block in
# app/build.gradle.kts, which pairs this file with AGP's `proguard-android-optimize.txt`).
#
# Both distribution flavors ("github" and "playstore") are minified, shrunk and obfuscated with
# this exact configuration - the flavors only differ in the self-update flow, not in shrinking.
#
# Rule of thumb when adding to this file: only keep what is reached *by name at runtime*
# (reflection, JNI, or a name the platform resolves out of the manifest). Everything this app
# resolves through normal Kotlin calls - Koin's `single { }`/`get()` DSL, use cases, ViewModels,
# domain models, Compose UI - is renamed consistently by R8 at both the definition and the call
# site, so keeping it only makes the APK bigger and the obfuscation weaker.

# ── Crash readability ────────────────────────────────────────────────────────
# Obfuscated stack traces are unreadable without these: keep file/line info in the trace, but
# rename every source file to a single "SourceFile" placeholder so the original .kt filenames
# (which leak the package/class layout obfuscation is meant to hide) are not shipped. Traces are
# then de-obfuscated with the R8 mapping file - app/build/outputs/mapping/<flavor>Release/
# mapping.txt, published as a GitHub Release asset and embedded in the Play App Bundle by AGP
# (see .github/workflows/release.yml).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Obfuscation strength / size ──────────────────────────────────────────────
# Flatten every renamed class into the root package. Nothing in this app resolves a class from a
# package name (the single name-based lookup is Room's, and it is pinned by the -keep below), so
# the package hierarchy carries no runtime meaning in the release build.
-repackageclasses ''
# Lets R8 widen member/class visibility so it can inline and merge across package boundaries -
# safe here because the whole program is processed in one R8 invocation and nothing depends on
# reflective access checks.
-allowaccessmodification

# ── Room ─────────────────────────────────────────────────────────────────────
# Room resolves the KSP-generated `FamilyMomentsDatabase_Impl` reflectively, by appending "_Impl"
# to the runtime name of the class handed to `Room.databaseBuilder(...)` (see
# core/data/.../data/di/DataModule.kt). Renaming either half independently breaks that lookup, so
# both the abstract database class and its generated subclass - which also matches this rule,
# since it extends RoomDatabase transitively - keep their original names and no-arg constructor.
# room-runtime ships this same rule as a consumer rule; it is repeated here so the app's own
# `Class.forName` contract stays visible next to the config that enables obfuscation.
-keep class * extends androidx.room.RoomDatabase { <init>(); }

# ── Koin ─────────────────────────────────────────────────────────────────────
# Koin's DSL (`single { }`, `factory { }`, `get()`, `by inject()`) resolves definitions from
# `KClass` literals captured at compile time, so obfuscated app classes match fine - Koin itself
# needs no keep rule to work. This is deliberately -keepnames (= keep, allowshrinking) rather than
# a blanket -keep: every Koin name that survives is unchanged, so any name-based lookup behaves
# exactly as it does unobfuscated, while the large parts of the DI runtime this app never touches
# can still be shrunk away.
-keepnames class org.koin.** { *; }

# ── Coroutines ───────────────────────────────────────────────────────────────
# kotlinx-coroutines loads these two through ServiceLoader/reflection. The coroutines artifacts
# ship the same rules in META-INF/proguard, kept here so the dependency is explicit.
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
