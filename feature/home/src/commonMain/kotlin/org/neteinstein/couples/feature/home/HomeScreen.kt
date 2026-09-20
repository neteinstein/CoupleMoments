package org.neteinstein.couples.feature.home

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
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import couplemoments.feature.home.generated.resources.Res
import couplemoments.feature.home.generated.resources.cancel
import couplemoments.feature.home.generated.resources.category_all
import couplemoments.feature.home.generated.resources.category_daily_life
import couplemoments.feature.home.generated.resources.category_future_dreams
import couplemoments.feature.home.generated.resources.category_ice_breakers
import couplemoments.feature.home.generated.resources.category_intimacy
import couplemoments.feature.home.generated.resources.category_memories
import couplemoments.feature.home.generated.resources.category_values
import couplemoments.feature.home.generated.resources.cd_close
import couplemoments.feature.home.generated.resources.cd_filter_by_category
import couplemoments.feature.home.generated.resources.cd_settings
import couplemoments.feature.home.generated.resources.cd_shuffle_card
import couplemoments.feature.home.generated.resources.cd_switch_to_grid_view
import couplemoments.feature.home.generated.resources.cd_switch_to_swipe_view
import couplemoments.feature.home.generated.resources.hide_card_message
import couplemoments.feature.home.generated.resources.hide_card_title
import couplemoments.feature.home.generated.resources.home_app_name
import couplemoments.feature.home.generated.resources.home_no_cards_subtitle
import couplemoments.feature.home.generated.resources.home_no_cards_title
import couplemoments.feature.home.generated.resources.home_subtitle
import couplemoments.feature.home.generated.resources.home_swipe_hint
import couplemoments.feature.home.generated.resources.home_take_turns
import couplemoments.feature.home.generated.resources.home_vertical_swipe_hint
import couplemoments.feature.home.generated.resources.intimacy_gate_confirm
import couplemoments.feature.home.generated.resources.intimacy_gate_dismiss
import couplemoments.feature.home.generated.resources.intimacy_gate_message
import couplemoments.feature.home.generated.resources.intimacy_gate_title
import couplemoments.feature.home.generated.resources.yes
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.neteinstein.couples.domain.model.Question
import org.neteinstein.couples.domain.model.QuestionCategory
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

@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var swipeDirection by remember { mutableIntStateOf(0) } // -1 left, +1 right, 0 none
    var fullScreenQuestion by remember { mutableStateOf<Question?>(null) }
    // Keeps showing the last opened question while the close animation fades/scales it out,
    // instead of the content blanking out the instant fullScreenQuestion is cleared.
    var lastFullScreenQuestion by remember { mutableStateOf<Question?>(null) }
    if (fullScreenQuestion != null) {
        lastFullScreenQuestion = fullScreenQuestion
    }
    var isGridView by remember { mutableStateOf(false) }
    var showHideConfirmDialog by remember { mutableStateOf(false) }
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
        // The same four actions the card's swipe gestures trigger, also reachable from the arrow keys -
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
                        onDown = {
                            if (fullScreenQuestion == null) showHideConfirmDialog = true
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
                // Top bar
                HomeTopBar(
                    onShuffleClick = {
                        revealOrigin = null
                        uiState.questions.randomOrNull()?.let { fullScreenQuestion = it }
                    },
                    shuffleEnabled = uiState.questions.isNotEmpty(),
                    onSettingsClick = onSettingsClick,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subtitle
                if (!isGridView) {
                    SwipeHint(
                        text = stringResource(Res.string.home_swipe_hint),
                        style = MaterialTheme.typography.labelMedium,
                        trailingIcons = listOf(Icons.AutoMirrored.Filled.ArrowBack, Icons.AutoMirrored.Filled.ArrowForward),
                    )
                    SwipeHint(
                        text = stringResource(Res.string.home_vertical_swipe_hint),
                        style = MaterialTheme.typography.labelSmall,
                        trailingIcons = listOf(Icons.Default.KeyboardArrowUp, Icons.Default.KeyboardArrowDown),
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (isGridView) {
                    QuestionGrid(
                        questions = uiState.questions,
                        onQuestionClick = { question, bounds ->
                            revealOrigin = bounds
                            fullScreenQuestion = question
                        },
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    QuestionCard(
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
                        onSwipeDown = { showHideConfirmDialog = true },
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Progress indicator dots (above) + category filter (bottom left) below,
                // stacked vertically so a wide category label never overlaps the dots.
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (!isGridView && !uiState.isLoading && uiState.totalQuestions > 0) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            ProgressDots(
                                current = uiState.currentIndex,
                                total = uiState.totalQuestions,
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CategoryDropdown(
                            selectedCategory = uiState.selectedCategory,
                            onCategorySelected = viewModel::onCategorySelected,
                            modifier = Modifier.align(Alignment.CenterStart),
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
                    FullScreenQuestion(
                        question = lastFullScreenQuestion,
                        onClose = { fullScreenQuestion = null },
                        onRandomClick = { uiState.questions.randomOrNull()?.let { fullScreenQuestion = it } },
                        randomEnabled = uiState.questions.isNotEmpty(),
                    )
                }
            }
        }
    }

    if (showHideConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showHideConfirmDialog = false },
            title = { Text(stringResource(Res.string.hide_card_title)) },
            text = { Text(stringResource(Res.string.hide_card_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showHideConfirmDialog = false
                        viewModel.markCurrentQuestionAsUsed()
                    },
                ) {
                    Text(stringResource(Res.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showHideConfirmDialog = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }

    if (uiState.showIntimacyGate) {
        AlertDialog(
            onDismissRequest = viewModel::onIntimacyGateDismissed,
            title = { Text(stringResource(Res.string.intimacy_gate_title)) },
            text = { Text(stringResource(Res.string.intimacy_gate_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::onIntimacyGateConfirmed) {
                    Text(stringResource(Res.string.intimacy_gate_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onIntimacyGateDismissed) {
                    Text(stringResource(Res.string.intimacy_gate_dismiss))
                }
            },
        )
    }
}

@Composable
private fun FullScreenQuestion(
    question: Question?,
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
                question?.category?.let { category ->
                    CategoryPill(category = category)
                    Spacer(modifier = Modifier.height(24.dp))
                }
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
private fun HomeTopBar(
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
                text = stringResource(Res.string.home_app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(Res.string.home_subtitle),
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
private fun QuestionCard(
    uiState: HomeUiState,
    swipeDirection: Int,
    modifier: Modifier = Modifier,
    onCardPositioned: (Rect) -> Unit = {},
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
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
            label = "questionCard",
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
                                            isVerticalSwipe && offsetY > VERTICAL_SWIPE_THRESHOLD -> {
                                                onSwipeDown()
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
                    if (question == null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp),
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = stringResource(Res.string.home_no_cards_title),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(Res.string.home_no_cards_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            CategoryPill(category = question.category)
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
                                text = stringResource(Res.string.home_take_turns),
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
private fun QuestionGrid(
    questions: List<Question>,
    onQuestionClick: (Question, Rect) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (questions.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Inbox,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(Res.string.home_no_cards_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(Res.string.home_no_cards_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(questions, key = { it.id }) { question ->
            GridQuestionCard(
                question = question,
                onClick = { bounds -> onQuestionClick(question, bounds) },
            )
        }
    }
}

@Composable
private fun GridQuestionCard(
    question: Question,
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
            // The full CategoryPill label (icon + name) is too wide for a 3-column card and
            // would get clipped by the card's rounded corners, so just show the icon here.
            Icon(
                imageVector = question.category.icon(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.TopEnd).size(14.dp),
            )
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

private fun categoryLabelRes(category: QuestionCategory): StringResource =
    when (category) {
        is QuestionCategory.IceBreakers -> Res.string.category_ice_breakers
        is QuestionCategory.Memories -> Res.string.category_memories
        is QuestionCategory.Values -> Res.string.category_values
        is QuestionCategory.FutureDreams -> Res.string.category_future_dreams
        is QuestionCategory.DailyLife -> Res.string.category_daily_life
        is QuestionCategory.Intimacy -> Res.string.category_intimacy
        else -> Res.string.category_ice_breakers
    }

// A plain function (not @Composable) mapping each category to its icon - see QuestionCategory's
// kdoc for why the icon is chosen here in the UI layer rather than carried on the model, and why
// these are vector icons rather than the emoji glyphs this screen used before.
private fun QuestionCategory.icon(): ImageVector =
    when (this) {
        is QuestionCategory.IceBreakers -> Icons.Default.Celebration
        is QuestionCategory.Memories -> Icons.Default.PhotoCamera
        is QuestionCategory.Values -> Icons.Default.Favorite
        is QuestionCategory.FutureDreams -> Icons.Default.AutoAwesome
        is QuestionCategory.DailyLife -> Icons.Default.WbSunny
        is QuestionCategory.Intimacy -> Icons.Default.Nightlight
        else -> Icons.Default.Celebration
    }

@Composable
private fun categoryDisplayLabel(category: QuestionCategory): String = stringResource(categoryLabelRes(category))

@Composable
private fun CategoryPill(
    category: QuestionCategory,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = category.icon(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = categoryDisplayLabel(category),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun CategoryDropdown(
    selectedCategory: QuestionCategory?,
    onCategorySelected: (QuestionCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val allLabel = stringResource(Res.string.category_all)
    // Deliberately unrolled instead of looping over QuestionCategory.all: calling a @Composable
    // function with an argument sourced from a loop/forEach/map variable reproducibly corrupted
    // that argument under this project's exact Kotlin/Compose compiler version (see git history
    // on this file for the failed alternatives). Direct references to the five known singletons
    // sidestep it entirely, and the category list is small and fixed, so unrolling is cheap.
    val iceBreakersLabel = QuestionCategory.IceBreakers to categoryDisplayLabel(QuestionCategory.IceBreakers)
    val memoriesLabel = QuestionCategory.Memories to categoryDisplayLabel(QuestionCategory.Memories)
    val valuesLabel = QuestionCategory.Values to categoryDisplayLabel(QuestionCategory.Values)
    val futureDreamsLabel = QuestionCategory.FutureDreams to categoryDisplayLabel(QuestionCategory.FutureDreams)
    val dailyLifeLabel = QuestionCategory.DailyLife to categoryDisplayLabel(QuestionCategory.DailyLife)
    val intimacyLabel = QuestionCategory.Intimacy to categoryDisplayLabel(QuestionCategory.Intimacy)
    val categoryLabels =
        listOf(iceBreakersLabel, memoriesLabel, valuesLabel, futureDreamsLabel, dailyLifeLabel, intimacyLabel)
    val selectedLabel = categoryLabels.firstOrNull { (category, _) -> category == selectedCategory }?.second ?: allLabel

    Box(modifier = modifier) {
        Row(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { expanded = true }
                    .onGloballyPositioned { anchorHeightPx = it.size.height }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = selectedLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = stringResource(Res.string.cd_filter_by_category),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(0.dp, -with(density) { anchorHeightPx.toDp() }),
            properties = PopupProperties(focusable = true, clippingEnabled = false),
        ) {
            DropdownMenuItem(
                text = { Text(allLabel) },
                onClick = {
                    expanded = false
                    onCategorySelected(null)
                },
            )
            for ((category, label) in categoryLabels) {
                DropdownMenuItem(
                    text = { Text(label) },
                    leadingIcon = {
                        Icon(imageVector = category.icon(), contentDescription = null, modifier = Modifier.size(20.dp))
                    },
                    onClick = {
                        expanded = false
                        onCategorySelected(category)
                    },
                )
            }
        }
    }
}

// Capped so a category with many cards doesn't lay out (and re-measure on every swipe) an
// unbounded row of dots - core:ui's PagerIndicator windows dotCount dots around the current page,
// so this stays a small fixed-size row no matter how large total gets.
private const val MAX_VISIBLE_DOTS = 7

@Composable
private fun ProgressDots(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    if (total <= 0) return
    // The deck loops through every card in the category (nextQuestion/previousQuestion wrap via
    // modulo), so the indicator's page count/current page are derived from the wrapped index
    // rather than the raw, ever-increasing current.
    val visibleIndex = current % total
    // A quick scale burst gives the active dot a tactile pop whenever the current card changes -
    // core:ui's PagerIndicator snaps between dots rather than spring-morphing (see its doc comment),
    // so this pulse is this screen's own added motion on top of it.
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
        dotCount = minOf(total, MAX_VISIBLE_DOTS),
        activeDotColor = MaterialTheme.colorScheme.primary,
        dotColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        modifier =
            modifier.graphicsLayer {
                scaleX = pulseScale.value
                scaleY = pulseScale.value
            },
    )
}

/**
 * A hint line followed by a couple of small trailing direction icons. Those arrows used to be
 * literal "←"/"→"/"↑"/"↓" characters inside the hint strings themselves, which render as "tofu"
 * boxes on the Wasm/Skia web build - see QuestionCategory's kdoc for the same problem and fix.
 */
@Composable
private fun SwipeHint(
    text: String,
    style: TextStyle,
    trailingIcons: List<ImageVector>,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = style,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        trailingIcons.forEach { icon ->
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}
