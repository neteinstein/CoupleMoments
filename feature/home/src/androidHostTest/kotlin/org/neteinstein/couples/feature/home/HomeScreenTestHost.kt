package org.neteinstein.couples.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import org.jetbrains.compose.resources.PreviewContextConfigurationEffect
import org.neteinstein.couples.ui.theme.CoupleMomentsTheme

/**
 * Robolectric doesn't initialize Compose Multiplatform's composeResources
 * `AndroidContextProvider` ContentProvider the way a real app process does, so
 * `stringResource()`/`painterResource()` (used throughout HomeScreen) crash under
 * `createComposeRule()` with a bare RuntimeException unless the context is set manually - via the
 * same mechanism Compose Multiplatform itself uses to make `@Preview` rendering work in Android
 * Studio, since that has the identical problem. See
 * https://github.com/robolectric/robolectric/issues/9603 and
 * https://youtrack.jetbrains.com/issue/CMP-6612.
 */
@Composable
internal fun HomeScreenTestHost(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalInspectionMode provides true) {
        PreviewContextConfigurationEffect()
    }
    CoupleMomentsTheme(darkTheme = false, dynamicColor = false) {
        content()
    }
}
