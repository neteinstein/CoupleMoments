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
 * Replacement for `mx.platacard:compose-pager-indicator` (used by `feature:home`/`feature:game`'s
 * `ProgressDots`) - that artifact has no multiplatform build. [dotCount] windows the row to at
 * most that many dots, centered on [currentPage], matching `mx.platacard`'s behavior of keeping a
 * fixed-size row for decks with many pages. Unlike `mx.platacard`'s `currentPageFraction`, page
 * changes snap rather than spring-morph between dots - a deliberate, documented trade-off for the
 * KMP migration's Android-only feature-module conversion step (see the migration plan's rollout
 * steps); callers layer their own scale/pulse animation on top via [modifier] if they want extra
 * motion on page change (see `ProgressDots` in `feature/home/.../HomeScreen.kt`).
 */
@Composable
fun PagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    dotCount: Int = pageCount,
    activeDotColor: Color = MaterialTheme.colorScheme.primary,
    dotColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
    dotSize: Dp = 8.dp,
    activeDotSize: Dp = 10.dp,
    spacing: Dp = 6.dp,
) {
    if (pageCount <= 0) return
    val windowSize = dotCount.coerceIn(1, pageCount)
    val maxWindowStart = (pageCount - windowSize).coerceAtLeast(0)
    val windowStart = (currentPage - windowSize / 2).coerceIn(0, maxWindowStart)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        repeat(windowSize) { offset ->
            val page = windowStart + offset
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
