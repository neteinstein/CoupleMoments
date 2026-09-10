package org.neteinstein.couples.feature.splash

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import org.neteinstein.couples.ui.R

/** Renders the same campfire-scene artwork used by the launcher icon, so the splash screen matches it. */
@Composable
fun CoupleMomentsLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.ic_campfire_scene),
        contentDescription = null,
        modifier = modifier,
    )
}
