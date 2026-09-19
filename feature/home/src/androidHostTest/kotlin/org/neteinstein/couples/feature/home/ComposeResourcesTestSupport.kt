package org.neteinstein.couples.feature.home

import android.content.ContentProvider
import org.robolectric.android.controller.ContentProviderController

/**
 * Robolectric doesn't auto-initialize Compose Multiplatform's composeResources
 * `AndroidContextProvider` ContentProvider (declared in the compose resources library's own
 * AndroidManifest, merged into this module's test manifest) the way a real app process does, so
 * `stringResource()`/`painterResource()` (used throughout HomeScreen) crash under
 * `createComposeRule()` with a bare RuntimeException unless it's attached manually - the same way
 * Robolectric expects any other manifest-declared ContentProvider to be initialized in a test that
 * needs it. `AndroidContextProvider` itself is internal to the library (not directly importable),
 * so it's reached by class name via reflection.
 * See https://github.com/robolectric/robolectric/issues/9603.
 */
internal fun initComposeResourcesTestContext() {
    val provider =
        Class
            .forName("org.jetbrains.compose.resources.AndroidContextProvider")
            .getDeclaredConstructor()
            .newInstance() as ContentProvider
    ContentProviderController.of(provider).create()
}
