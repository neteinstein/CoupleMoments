package org.neteinstein.couples.ui.animation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.lerp

/**
 * Grows/shrinks [content] from [origin]'s rect (in root coordinates, e.g. from
 * `LayoutCoordinates.boundsInRoot()`) instead of a fixed center-anchored scale, so a full-screen
 * overlay visually emerges from the card/tile that opened it. Must be called from inside an
 * [AnimatedVisibilityScope] with that visibility's own `enter`/`exit` set to `None` - this drives
 * the reveal off the same [AnimatedVisibilityScope.transition], so the overlay isn't disposed
 * until this animation (not just the caller's) finishes.
 *
 * [origin] is captured once, by the caller, at the moment the overlay is requested, and null
 * falls back to a plain fade + gentle scale from the overlay's own center.
 */
@Composable
fun AnimatedVisibilityScope.OriginReveal(
    origin: Rect?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var overlaySize by remember { mutableStateOf(IntSize.Zero) }
    val progress by transition.animateFloat(
        label = "originReveal",
        transitionSpec = { tween(durationMillis = 380, easing = FastOutSlowInEasing) },
    ) { state -> if (state == EnterExitState.Visible) 1f else 0f }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .onGloballyPositioned { overlaySize = it.size }
                .graphicsLayer {
                    if (origin != null && overlaySize.width > 0 && overlaySize.height > 0) {
                        val overlayCenter =
                            Rect(0f, 0f, overlaySize.width.toFloat(), overlaySize.height.toFloat()).center
                        val startScaleX = (origin.width / overlaySize.width).coerceIn(0.05f, 1f)
                        val startScaleY = (origin.height / overlaySize.height).coerceIn(0.05f, 1f)
                        scaleX = lerp(startScaleX, 1f, progress)
                        scaleY = lerp(startScaleY, 1f, progress)
                        val startCenter = origin.center
                        translationX = lerp(startCenter.x - overlayCenter.x, 0f, progress)
                        translationY = lerp(startCenter.y - overlayCenter.y, 0f, progress)
                        alpha = lerp(0.3f, 1f, progress)
                    } else {
                        val fallbackScale = lerp(0.85f, 1f, progress)
                        scaleX = fallbackScale
                        scaleY = fallbackScale
                        alpha = progress
                    }
                },
    ) {
        content()
    }
}
