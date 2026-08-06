package org.neteinstein.family.feature.splash

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import org.neteinstein.family.ui.R

/** Renders the same campfire-scene artwork used by the launcher icon, so the splash screen matches it. */
@Composable
fun FamilyMomentsLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.ic_campfire_scene),
        contentDescription = null,
        modifier = modifier
    )
}
