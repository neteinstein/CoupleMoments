package org.neteinstein.couples.feature.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import couplemoments.feature.splash.generated.resources.Res
import couplemoments.feature.splash.generated.resources.splash_app_name
import couplemoments.feature.splash.generated.resources.splash_loopgain_footer
import couplemoments.feature.splash.generated.resources.splash_tagline
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val scale = remember { Animatable(0.4f) }
    val alpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    // A gentle heartbeat (double-thump, like the heart drawn at the logo's infinity crossing)
    // that keeps beating for as long as the splash is on screen, layered on top of the one-shot
    // entrance scale below rather than replacing it.
    val heartbeat = rememberInfiniteTransition(label = "logoHeartbeat")
    val heartbeatScale by heartbeat.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    keyframes {
                        durationMillis = 1000
                        1f at 0
                        1.14f at 120 using FastOutSlowInEasing
                        1f at 260 using FastOutSlowInEasing
                        1.07f at 380 using FastOutSlowInEasing
                        1f at 500 using FastOutSlowInEasing
                    },
            ),
        label = "heartbeatScale",
    )

    LaunchedEffect(Unit) {
        // Logo scale + fade in
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500),
        )
        // Text fades in after logo
        delay(200)
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600),
        )
        // Hold and navigate
        delay(1200)
        onSplashFinished()
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.background,
                                ),
                        ),
                ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CoupleMomentsLogo(
                modifier =
                    Modifier
                        .size(160.dp)
                        // Only layer the continuous heartbeat on top once the one-shot entrance
                        // scale has essentially settled, so the two don't visibly fight.
                        .scale(scale.value * (if (scale.value > 0.98f) heartbeatScale else 1f))
                        .alpha(alpha.value),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(Res.string.splash_app_name),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                // Matches the app title's color on the Questions/Game tabs.
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.alpha(textAlpha.value),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.splash_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(textAlpha.value),
            )
        }

        Text(
            text = stringResource(Res.string.splash_loopgain_footer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .alpha(textAlpha.value),
        )
    }
}
