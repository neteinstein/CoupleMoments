package org.neteinstein.family.feature.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInHorizontally
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.ViewCarousel
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.abs
import kotlin.math.roundToInt
import mx.platacard.pagerindicator.PagerIndicator
import org.koin.androidx.compose.koinViewModel
import org.neteinstein.family.domain.model.Question
import org.neteinstein.family.domain.model.QuestionCategory

private const val SWIPE_THRESHOLD = 100f
private const val VERTICAL_SWIPE_THRESHOLD = 120f
private const val MAX_VERTICAL_NUDGE = 140f

@Composable
fun HomeScreen(onSettingsClick: () -> Unit, viewModel: HomeViewModel = koinViewModel()) {
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

    LaunchedEffect(Unit) {
        viewModel.onScreenEntered()
    }

    BackHandler(enabled = fullScreenQuestion != null) { fullScreenQuestion = null }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Decorative background gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.background
                            ),
                            radius = 900f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top bar
                HomeTopBar(
                    onShuffleClick = { uiState.questions.randomOrNull()?.let { fullScreenQuestion = it } },
                    shuffleEnabled = uiState.questions.isNotEmpty(),
                    onSettingsClick = onSettingsClick
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subtitle
                if (!isGridView) {
                    Text(
                        text = stringResource(R.string.home_swipe_hint),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.home_vertical_swipe_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (isGridView) {
                    QuestionGrid(
                        questions = uiState.questions,
                        onQuestionClick = { fullScreenQuestion = it },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    QuestionCard(
                        uiState = uiState,
                        swipeDirection = swipeDirection,
                        modifier = Modifier.weight(1f),
                        onSwipeLeft = {
                            swipeDirection = -1
                            viewModel.nextQuestion()
                        },
                        onSwipeRight = {
                            swipeDirection = 1
                            viewModel.previousQuestion()
                        },
                        onSwipeUp = { fullScreenQuestion = uiState.currentQuestion },
                        onSwipeDown = { showHideConfirmDialog = true }
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
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CategoryDropdown(
                            selectedCategory = uiState.selectedCategory,
                            onCategorySelected = viewModel::onCategorySelected,
                            modifier = Modifier.align(Alignment.CenterStart)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // View toggle (bottom right corner), hidden while a card is shown full screen.
            IconButton(
                onClick = { isGridView = !isGridView },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(16.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = if (isGridView) Icons.Default.ViewCarousel else Icons.Default.GridView,
                    contentDescription = stringResource(
                        if (isGridView) R.string.cd_switch_to_swipe_view else R.string.cd_switch_to_grid_view
                    ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = fullScreenQuestion != null,
                enter = fadeIn(tween(200)) +
                    expandIn(
                        animationSpec = tween(400, easing = FastOutSlowInEasing),
                        expandFrom = Alignment.Center
                    ) { fullSize -> IntSize(fullSize.width, (fullSize.height * 0.35f).roundToInt()) },
                exit = fadeOut(tween(200)) +
                    shrinkOut(
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                        shrinkTowards = Alignment.Center
                    ) { fullSize -> IntSize(fullSize.width, (fullSize.height * 0.35f).roundToInt()) },
                modifier = Modifier.fillMaxSize()
            ) {
                FullScreenQuestion(
                    question = lastFullScreenQuestion,
                    onClose = { fullScreenQuestion = null }
                )
            }
        }
    }

    if (showHideConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showHideConfirmDialog = false },
            title = { Text(stringResource(R.string.hide_card_title)) },
            text = { Text(stringResource(R.string.hide_card_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showHideConfirmDialog = false
                        viewModel.markCurrentQuestionAsUsed()
                    }
                ) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showHideConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun FullScreenQuestion(question: Question?, onClose: () -> Unit) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    Surface(
        modifier = Modifier
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
                    }
                )
            },
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(16.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.cd_close),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                question?.category?.let { category ->
                    CategoryPill(category = category)
                    Spacer(modifier = Modifier.height(24.dp))
                }
                Text(
                    text = "💬",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = question?.text ?: "",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = MaterialTheme.typography.headlineMedium.lineHeight * 1.2f
                )
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    onShuffleClick: () -> Unit,
    shuffleEnabled: Boolean,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(R.string.home_app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = onShuffleClick,
                enabled = shuffleEnabled,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = stringResource(R.string.cd_shuffle_card),
                    tint = if (shuffleEnabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    }
                )
            }
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.cd_settings),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val cardRotation by animateFloatAsState(
        targetValue = offsetX * 0.04f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "cardRotation"
    )
    val cardOffsetY by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "cardOffsetY"
    )

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = uiState.currentQuestion,
            transitionSpec = {
                if (swipeDirection <= 0) {
                    (slideInHorizontally(tween(400)) { it } + fadeIn(tween(300))) togetherWith
                        (slideOutHorizontally(tween(400)) { -it } + fadeOut(tween(300)))
                } else {
                    (slideInHorizontally(tween(400)) { -it } + fadeIn(tween(300))) togetherWith
                        (slideOutHorizontally(tween(400)) { it } + fadeOut(tween(300)))
                }
            },
            label = "questionCard"
        ) { question ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .offset { IntOffset(0, cardOffsetY.roundToInt()) }
                    .rotate(cardRotation)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                val isVerticalSwipe = abs(offsetY) > abs(offsetX)
                                when {
                                    isVerticalSwipe && offsetY < -VERTICAL_SWIPE_THRESHOLD -> onSwipeUp()
                                    isVerticalSwipe && offsetY > VERTICAL_SWIPE_THRESHOLD -> onSwipeDown()
                                    !isVerticalSwipe && offsetX < -SWIPE_THRESHOLD -> onSwipeLeft()
                                    !isVerticalSwipe && offsetX > SWIPE_THRESHOLD -> onSwipeRight()
                                }
                                offsetX = 0f
                                offsetY = 0f
                            },
                            onDragCancel = {
                                offsetX = 0f
                                offsetY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x
                                offsetY = (offsetY + dragAmount.y).coerceIn(-MAX_VERTICAL_NUDGE, MAX_VERTICAL_NUDGE)
                            }
                        )
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                                )
                            )
                        )
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (question == null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "🗂️",
                                style = MaterialTheme.typography.displaySmall
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = stringResource(R.string.home_no_cards_title),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.home_no_cards_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        question.category.let { category ->
                            CategoryPill(
                                category = category,
                                modifier = Modifier.align(Alignment.TopEnd)
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "💬",
                                style = MaterialTheme.typography.displaySmall
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = question.text,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = MaterialTheme.typography.headlineSmall.lineHeight * 1.2f
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                text = stringResource(R.string.home_take_turns),
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
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
    onQuestionClick: (Question) -> Unit,
    modifier: Modifier = Modifier
) {
    if (questions.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🗂️",
                    style = MaterialTheme.typography.displaySmall
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.home_no_cards_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.home_no_cards_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(questions, key = { it.id }) { question ->
            GridQuestionCard(
                question = question,
                onClick = { onQuestionClick(question) }
            )
        }
    }
}

@Composable
private fun GridQuestionCard(question: Question, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(0.75f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                        )
                    )
                )
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            // The full CategoryPill label (emoji + name) is too wide for a 3-column card and
            // would get clipped by the card's rounded corners, so just show the emoji here.
            Text(
                text = question.category.emoji,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.TopEnd)
            )
            Text(
                text = question.text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun categoryLabelRes(category: QuestionCategory): Int {
    return when (category) {
        is QuestionCategory.IceBreakers -> R.string.category_ice_breakers
        is QuestionCategory.Memories -> R.string.category_memories
        is QuestionCategory.Values -> R.string.category_values
        is QuestionCategory.FutureDreams -> R.string.category_future_dreams
        is QuestionCategory.DailyLife -> R.string.category_daily_life
        else -> R.string.category_ice_breakers
    }
}

@Composable
private fun categoryDisplayLabel(category: QuestionCategory): String {
    return category.emoji + " " + stringResource(categoryLabelRes(category))
}

@Composable
private fun CategoryPill(category: QuestionCategory, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = categoryDisplayLabel(category),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1
        )
    }
}

@Composable
private fun CategoryDropdown(
    selectedCategory: QuestionCategory?,
    onCategorySelected: (QuestionCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val allLabel = stringResource(R.string.category_all)
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
    val categoryLabels = listOf(iceBreakersLabel, memoriesLabel, valuesLabel, futureDreamsLabel, dailyLifeLabel)
    val selectedLabel = categoryLabels.firstOrNull { (category, _) -> category == selectedCategory }?.second ?: allLabel

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { expanded = true }
                .onGloballyPositioned { anchorHeightPx = it.size.height }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = stringResource(R.string.cd_filter_by_category),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(0.dp, -with(density) { anchorHeightPx.toDp() }),
            properties = PopupProperties(focusable = true, clippingEnabled = false)
        ) {
            DropdownMenuItem(
                text = { Text(allLabel) },
                onClick = {
                    expanded = false
                    onCategorySelected(null)
                }
            )
            for ((category, label) in categoryLabels) {
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        expanded = false
                        onCategorySelected(category)
                    }
                )
            }
        }
    }
}

// Capped so a category with many cards doesn't lay out (and re-measure on every swipe) an
// unbounded row of dots - mx.platacard's PagerIndicator windows dotCount dots around the
// current page, so this stays a small fixed-size row no matter how large total gets.
private const val MAX_VISIBLE_DOTS = 7

@Composable
private fun ProgressDots(current: Int, total: Int, modifier: Modifier = Modifier) {
    if (total <= 0) return
    // The deck loops through every card in the category (nextQuestion/previousQuestion wrap via
    // modulo), so the indicator's page count/fraction are derived from the wrapped index rather
    // than the raw, ever-increasing current.
    val visibleIndex = current % total
    PagerIndicator(
        pageCount = total,
        currentPageFraction = rememberUpdatedState(visibleIndex.toFloat()),
        activeDotColor = MaterialTheme.colorScheme.primary,
        dotColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        dotCount = minOf(total, MAX_VISIBLE_DOTS),
        modifier = modifier
    )
}
