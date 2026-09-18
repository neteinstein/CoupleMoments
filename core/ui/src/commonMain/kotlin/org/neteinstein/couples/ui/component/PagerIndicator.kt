package org.neteinstein.couples.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Groundwork for replacing `mx.platacard:compose-pager-indicator` (used today by
 * `feature:home`/`feature:game`'s `ProgressDots`, see
 * `feature/home/src/main/kotlin/org/neteinstein/couples/feature/home/HomeScreen.kt`) with a
 * hand-rolled, Compose Multiplatform-safe component - `mx.platacard`'s artifact has no
 * multiplatform build. NOT wired into any feature screen yet: `ProgressDots` currently needs
 * fractional-page animation (`currentPageFraction: State<Float>`, for the spring-driven morph
 * between dots as the deck swipes) and a fixed-size windowing of the dot row around the current
 * page for decks with many cards (`dotCount`/`MAX_VISIBLE_DOTS`) - reproducing that behavior
 * faithfully is deliberately out of scope for this step (see the KMP migration plan's rollout
 * steps for `feature:home`/`feature:game`). This is a simpler, integer-page building block only:
 * a plain row of dots with the current page highlighted, styled to roughly match `ProgressDots`'
 * `activeDotColor`/`dotColor` usage.
 *
 * Do not call this from `feature:home`/`feature:game` yet - those modules aren't converted to KMP
 * in this step, and swapping their pager indicator implementation is separate follow-up work.
 */
@Composable
fun PagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    activeDotColor: Color = MaterialTheme.colorScheme.primary,
    dotColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
    dotSize: Dp = 8.dp,
    activeDotSize: Dp = 10.dp,
    spacing: Dp = 6.dp,
) {
    if (pageCount <= 0) return
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        repeat(pageCount) { page ->
            val isActive = page == currentPage
            Box(
                modifier =
                    Modifier
                        .size(if (isActive) activeDotSize else dotSize)
                        .clip(CircleShape)
                        .background(if (isActive) activeDotColor else dotColor),
            )
        }
    }
}
