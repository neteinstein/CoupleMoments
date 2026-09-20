package org.neteinstein.couples.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.StorageSettings
import org.jetbrains.compose.resources.stringResource
import org.neteinstein.couples.resources.Res
import org.neteinstein.couples.resources.install_banner_action
import org.neteinstein.couples.resources.install_banner_dismiss
import org.neteinstein.couples.resources.install_banner_message

private const val DISMISSED_KEY = "install_banner_dismissed"
private const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=org.neteinstein.couples"

/**
 * Dismissal is persisted through `StorageSettings` - multiplatform-settings' `localStorage`
 * implementation, the same store every other web preference uses - so closing the banner sticks
 * across page reloads rather than reappearing on every visit.
 */
@Composable
actual fun PlatformInstallAppBanner() {
    val settings = remember { StorageSettings() }
    var dismissed by remember { mutableStateOf(settings.getBoolean(DISMISSED_KEY, false)) }
    if (dismissed) return

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Android,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = stringResource(Res.string.install_banner_message),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = { openUrl(PLAY_STORE_URL) }) {
            Text(
                text = stringResource(Res.string.install_banner_action),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        IconButton(
            onClick = {
                settings.putBoolean(DISMISSED_KEY, true)
                dismissed = true
            },
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(Res.string.install_banner_dismiss),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

// Opened in a new tab so the running app (and any card the visitor was on) isn't navigated away
// from. "noopener" is the standard hardening for a target=_blank link - it stops the opened page
// reaching back through window.opener.
private fun openUrl(url: String): Unit = js("window.open(url, '_blank', 'noopener')")
