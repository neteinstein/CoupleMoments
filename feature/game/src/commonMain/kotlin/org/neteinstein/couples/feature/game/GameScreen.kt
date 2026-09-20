package org.neteinstein.couples.feature.game

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.neteinstein.couples.feature.game.resources.Res
import org.neteinstein.couples.feature.game.resources.cd_close
import org.neteinstein.couples.feature.game.resources.cd_settings
import org.neteinstein.couples.feature.game.resources.cd_shuffle_card
import org.neteinstein.couples.feature.game.resources.cd_switch_to_grid_view
import org.neteinstein.couples.feature.game.resources.cd_switch_to_swipe_view
import org.neteinstein.couples.feature.game.resources.game_subtitle
import org.neteinstein.couples.feature.game.resources.game_swipe_hint
import org.neteinstein.couples.feature.game.resources.game_take_turns
import org.neteinstein.couples.feature.game.resources.game_title
import org.neteinstein.couples.feature.game.resources.game_top_bar_subtitle
import org.neteinstein.couples.ui.animation.OriginReveal
import org.neteinstein.couples.ui.component.PagerIndicator
import org.neteinstein.couples.ui.component.PlatformBackHandler
import org.neteinstein.couples.ui.input.arrowKeyNavigation
import kotlin.math.abs
import kotlin.math.roundToInt

private const val SWIPE_THRESHOLD = 100f
private const val VERTICAL_SWIPE_THRESHOLD = 120f
private const val MAX_VERTICAL_NUDGE = 140f

// How far behind the outgoing card the next one starts before growing into place.
private const val CARD_BEHIND_SCALE = 0.9f
private const val CARD_TRANSITION_MS = 350
private const val CARD_FADE_MS = 250
private const val MAX_VISIBLE_DOTS = 7

@Composable
fun GameScreen(
    onSettingsClick: () -> Unit,
    viewModel: GameViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var swipeDirection by remember { mutableIntStateOf(0) } // -1 left, +1 right, 0 none
    var fullScreenQuestion by remember { mutableStateOf<GameQuestion?>(null) }
    // Keeps showing the last opened question while the close animation fades/scales it out,
    // instead of the content blanking out the instant fullScreenQuestion is cleared.
    var lastFullScreenQuestion by remember { mutableStateOf<GameQuestion?>(null) }
    if (fullScreenQuestion != null) {
        lastFullScreenQuestion = fullScreenQuestion
    }
    var isGridView by remember { mutableStateOf(false) }
    // Bounds (in root coordinates) of whichever card/tile last opened the full-screen overlay, so
    // it can visually grow from there instead of always expanding from the screen's center.
    var deckCardBounds by remember { mutableStateOf<Rect?>(null) }
    var revealOrigin by remember { mutableStateOf<Rect?>(null) }

    LaunchedEffect(Unit) {
        viewModel.onScreenEntered()
    }

    PlatformBackHandler(enabled = fullScreenQuestion != null) { fullScreenQuestion = null }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        // Arrow-key equivalents of the card's swipe gestures. This screen has no
        // swipe-down-to-hide action, so only left/right/up are wired up, also reachable from the arrow keys -
        // there is no swipe gesture in a desktop browser, so without this the web build's deck can
        // only be moved by dragging with a mouse. Suppressed while a full-screen card is open so
        // the keys don't keep flipping the deck underneath it.
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .arrowKeyNavigation(
                        onPrevious = {
                            if (fullScreenQuestion == null) {
                                swipeDirection = 1
                                viewModel.previousQuestion()
                            }
                        },
                        onNext = {
                            if (fullScreenQuestion == null) {
                                swipeDirection = -1
                                viewModel.nextQuestion()
                            }
                        },
                        onUp = {
                            if (fullScreenQuestion == null) {
                                revealOrigin = deckCardBounds
                                fullScreenQuestion = uiState.currentQuestion
                            }
                        },
                    ),
        ) {
            // Decorative background gradient
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            brush =
                                Brush.radialGradient(
                                    colors =
                                        listOf(
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                            MaterialTheme.colorScheme.background,
                                        ),
                                    radius = 900f,
                                ),
                        ),
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                GameTopBar(
                    onShuffleClick = {
                        revealOrigin = null
                        uiState.questions.randomOrNull()?.let { fullScreenQuestion = it }
                    },
                    shuffleEnabled = uiState.questions.isNotEmpty(),
                    onSettingsClick = onSettingsClick,
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (!isGridView) {
                    // The "← →" arrows that used to be literal characters in this string render as
                    // "tofu" boxes on the Wasm/Skia web build - see QuestionCategory's kdoc.
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(Res.string.game_swipe_hint),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        listOf(Icons.AutoMirrored.Filled.ArrowBack, Icons.AutoMirrored.Filled.ArrowForward).forEach {
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (isGridView) {
                    GameQuestionGrid(
                        questions = uiState.questions,
                        onQuestionClick = { question, bounds ->
                            revealOrigin = bounds
                            fullScreenQuestion = question
                        },
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    GameQuestionCard(
                        uiState = uiState,
                        swipeDirection = swipeDirection,
                        modifier = Modifier.weight(1f),
                        onCardPositioned = { deckCardBounds = it },
                        onSwipeLeft = {
                            swipeDirection = -1
                            viewModel.nextQuestion()
                        },
                        onSwipeRight = {
                            swipeDirection = 1
                            viewModel.previousQuestion()
                        },
                        onSwipeUp = {
                            revealOrigin = deckCardBounds
                            fullScreenQuestion = uiState.currentQuestion
                        },
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (!isGridView && uiState.totalQuestions > 0) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        GameProgressDots(
                            current = uiState.currentIndex,
                            total = uiState.totalQuestions,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // View toggle (bottom right corner), hidden while a card is shown full screen.
            IconButton(
                onClick = { isGridView = !isGridView },
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(16.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Icon(
                    imageVector = if (isGridView) Icons.Default.ViewCarousel else Icons.Default.GridView,
                    contentDescription =
                        stringResource(
                            if (isGridView) Res.string.cd_switch_to_swipe_view else Res.string.cd_switch_to_grid_view,
                        ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AnimatedVisibility(
                visible = fullScreenQuestion != null,
                enter = EnterTransition.None,
                exit = ExitTransition.None,
                modifier = Modifier.fillMaxSize(),
            ) {
                OriginReveal(origin = revealOrigin) {
                    FullScreenGameQuestion(
                        question = lastFullScreenQuestion,
                        onClose = { fullScreenQuestion = null },
                        onRandomClick = { uiState.questions.randomOrNull()?.let { fullScreenQuestion = it } },
                        randomEnabled = uiState.questions.isNotEmpty(),
                    )
                }
            }
        }
    }
}

@Composable
private fun FullScreenGameQuestion(
    question: GameQuestion?,
    onClose: () -> Unit,
    onRandomClick: () -> Unit,
    randomEnabled: Boolean,
) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    Surface(
        modifier =
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            if (offsetY > VERTICAL_SWIPE_THRESHOLD) {
                                onClose()
                            }
                            offsetY = 0f
                        },
                        onDragCancel = { offsetY = 0f },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offsetY += dragAmount.y
                        },
                    )
                },
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = onClose,
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(16.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.cd_close),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(
                onClick = onRandomClick,
                enabled = randomEnabled,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(16.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = stringResource(Res.string.cd_shuffle_card),
                    tint =
                        if (randomEnabled) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        },
                )
            }

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                GameLabelPill()
                Spacer(modifier = Modifier.height(24.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp),
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = question?.text ?: "",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = MaterialTheme.typography.headlineMedium.lineHeight * 1.2f,
                )
            }
        }
    }
}

@Composable
private fun GameTopBar(
    onShuffleClick: () -> Unit,
    shuffleEnabled: Boolean,
    onSettingsClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = stringResource(Res.string.game_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(Res.string.game_top_bar_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = onShuffleClick,
                enabled = shuffleEnabled,
                modifier =
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = stringResource(Res.string.cd_shuffle_card),
                    tint =
                        if (shuffleEnabled) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        },
                )
            }
            IconButton(
                onClick = onSettingsClick,
                modifier =
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(Res.string.cd_settings),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun GameQuestionCard(
    uiState: GameUiState,
    swipeDirection: Int,
    modifier: Modifier = Modifier,
    onCardPositioned: (Rect) -> Unit = {},
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeUp: () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = uiState.currentQuestion,
            transitionSpec = {
                // The next card grows into place from behind (lower z-index) while the outgoing one
                // keeps travelling sideways in the direction it was flung, so it reads as a card
                // being pulled off the top of a deck rather than two cards crossing over.
                val exitTargetOffsetX = if (swipeDirection <= 0) { width: Int -> -width } else { width: Int -> width }
                (scaleIn(initialScale = CARD_BEHIND_SCALE, animationSpec = tween(CARD_TRANSITION_MS)) + fadeIn(tween(CARD_FADE_MS)))
                    .togetherWith(
                        slideOutHorizontally(tween(CARD_TRANSITION_MS), targetOffsetX = exitTargetOffsetX) +
                            fadeOut(tween(CARD_FADE_MS)),
                    ).apply { targetContentZIndex = -1f }
            },
            label = "gameQuestionCard",
        ) { question ->
            // Drag state lives per card instance, so the card that was just flung keeps the offset it
            // was released at and flies out from there instead of snapping back to center first.
            var offsetX by remember { mutableFloatStateOf(0f) }
            var offsetY by remember { mutableFloatStateOf(0f) }
            var isDragging by remember { mutableStateOf(false) }
            // The card follows the finger 1:1 while dragging; a release that didn't cross the swipe
            // threshold settles back to center with a little overshoot wobble.
            val settleSpec = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            val cardOffsetX by animateFloatAsState(
                targetValue = offsetX,
                animationSpec = if (isDragging) snap() else settleSpec,
                label = "cardOffsetX",
            )
            val cardOffsetY by animateFloatAsState(
                targetValue = offsetY,
                animationSpec = if (isDragging) snap() else settleSpec,
                label = "cardOffsetY",
            )
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .onGloballyPositioned { onCardPositioned(it.boundsInRoot()) }
                        .offset { IntOffset(cardOffsetX.roundToInt(), cardOffsetY.roundToInt()) }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { isDragging = true },
                                onDragEnd = {
                                    isDragging = false
                                    val isVerticalSwipe = abs(offsetY) > abs(offsetX)
                                    // Only a horizontal swipe on a deck with something else to show
                                    // actually replaces this card; every other outcome leaves it on
                                    // screen, so it has to travel back to center.
                                    val deckAdvances = uiState.questions.size > 1
                                    val flungAway =
                                        when {
                                            isVerticalSwipe && offsetY < -VERTICAL_SWIPE_THRESHOLD -> {
                                                onSwipeUp()
                                                false
                                            }
                                            !isVerticalSwipe && offsetX < -SWIPE_THRESHOLD -> {
                                                onSwipeLeft()
                                                deckAdvances
                                            }
                                            !isVerticalSwipe && offsetX > SWIPE_THRESHOLD -> {
                                                onSwipeRight()
                                                deckAdvances
                                            }
                                            else -> false
                                        }
                                    if (!flungAway) {
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                },
                                onDragCancel = {
                                    isDragging = false
                                    offsetX = 0f
                                    offsetY = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    offsetX += dragAmount.x
                                    offsetY = (offsetY + dragAmount.y).coerceIn(-MAX_VERTICAL_NUDGE, MAX_VERTICAL_NUDGE)
                                },
                            )
                        },
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                shape = RoundedCornerShape(24.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                brush =
                                    Brush.linearGradient(
                                        colors =
                                            listOf(
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                                            ),
                                    ),
                            ).padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (question != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            GameLabelPill()
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = question.text,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = MaterialTheme.typography.headlineSmall.lineHeight * 1.2f,
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                text = stringResource(Res.string.game_take_turns),
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameQuestionGrid(
    questions: List<GameQuestion>,
    onQuestionClick: (GameQuestion, Rect) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(questions, key = { it.id }) { question ->
            GridGameQuestionCard(
                question = question,
                onClick = { bounds -> onQuestionClick(question, bounds) },
            )
        }
    }
}

@Composable
private fun GridGameQuestionCard(
    question: GameQuestion,
    onClick: (Rect) -> Unit,
    modifier: Modifier = Modifier,
) {
    var bounds by remember { mutableStateOf(Rect.Zero) }
    Card(
        onClick = { onClick(bounds) },
        modifier =
            modifier
                .aspectRatio(0.75f)
                .onGloballyPositioned { bounds = it.boundsInRoot() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        brush =
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                                    ),
                            ),
                    ).padding(10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = question.text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun GameLabelPill(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = stringResource(Res.string.game_subtitle),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1,
        )
    }
}

@Composable
private fun GameProgressDots(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    if (total <= 0) return
    // The deck loops through every card (nextQuestion/previousQuestion wrap via modulo), so the
    // indicator's page count/current page are derived from the wrapped index rather than the raw,
    // ever-increasing current.
    val visibleIndex = current % total
    // A quick scale burst gives the active dot a tactile pop whenever the current card changes -
    // core:ui's PagerIndicator snaps between dots rather than spring-morphing (see its doc
    // comment), so this pulse is this screen's own added motion on top of it.
    val pulseScale = remember { Animatable(1f) }
    LaunchedEffect(visibleIndex) {
        pulseScale.snapTo(1f)
        pulseScale.animateTo(1.25f, animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing))
        pulseScale.animateTo(
            1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        )
    }
    PagerIndicator(
        pageCount = total,
        currentPage = visibleIndex,
        activeDotColor = MaterialTheme.colorScheme.primary,
        dotColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        dotCount = minOf(total, MAX_VISIBLE_DOTS),
        modifier =
            modifier.graphicsLayer {
                scaleX = pulseScale.value
                scaleY = pulseScale.value
            },
    )
}
