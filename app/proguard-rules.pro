# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Koin annotations
-keep class org.koin.** { *; }

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep data classes used in UI state
-keep class org.neteinstein.family.domain.model.** { *; }
-keep class org.neteinstein.family.feature.**.HomeUiState { *; }
