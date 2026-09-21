package org.neteinstein.couples.ui.input

import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

/**
 * Lets the arrow keys drive the card deck, so the web build is usable with a keyboard - there's no
 * swipe gesture on a desktop browser, which otherwise leaves the left/right card navigation
 * reachable only by dragging with a mouse. Left/right move between cards; [onUp]/[onDown] are
 * optional and drive the focus/hide gestures where a screen has them.
 *
 * Common code rather than a wasmJs-only actual: it costs nothing on Android/iOS (neither normally
 * has a hardware keyboard attached) and works for free on the ones that do - an Android tablet
 * with a keyboard case, or an iPad's Magic Keyboard.
 *
 * Grabs focus once on first composition so the keys work without the user clicking the deck first.
 * Pass [refocusOn] (any value that changes when an overlay opens or closes) to grab it again then:
 * clicking a control inside the overlay, or the one that just got removed, can otherwise leave
 * focus nowhere and the keys silently dead.
 */
@Composable
fun Modifier.arrowKeyNavigation(
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onUp: (() -> Unit)? = null,
    onDown: (() -> Unit)? = null,
    refocusOn: Any? = null,
): Modifier {
    val focusRequester = remember { FocusRequester() }
    // rememberUpdatedState so the handler below always calls the latest lambdas without being
    // rebuilt (and losing focus) every time the caller recomposes with new ones.
    val currentOnPrevious by rememberUpdatedState(onPrevious)
    val currentOnNext by rememberUpdatedState(onNext)
    val currentOnUp by rememberUpdatedState(onUp)
    val currentOnDown by rememberUpdatedState(onDown)

    LaunchedEffect(focusRequester, refocusOn) {
        // Throws if the node isn't attached/focusable yet (an overlay covering the deck, say) -
        // keyboard navigation is an enhancement, never a reason to crash the screen.
        runCatching { focusRequester.requestFocus() }
    }

    return this
        .focusRequester(focusRequester)
        .focusable()
        .onPreviewKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
            when (event.key) {
                Key.DirectionLeft -> {
                    currentOnPrevious()
                    true
                }
                Key.DirectionRight -> {
                    currentOnNext()
                    true
                }
                Key.DirectionUp ->
                    currentOnUp?.let {
                        it()
                        true
                    } ?: false
                Key.DirectionDown ->
                    currentOnDown?.let {
                        it()
                        true
                    } ?: false
                else -> false
            }
        }
}
