package org.neteinstein.couples.feature.splash

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import couplemoments.core.ui.generated.resources.Res
import couplemoments.core.ui.generated.resources.ic_couple_moments_mark
import org.jetbrains.compose.resources.painterResource

/** Renders the same mark used by the launcher icon, so the splash screen matches it. */
@Composable
fun CoupleMomentsLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.ic_couple_moments_mark),
        contentDescription = null,
        modifier = modifier,
    )
}
