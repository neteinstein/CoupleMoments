package org.neteinstein.family.feature.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

private const val LOOPGAIN_URL = "https://loopgain.org"
private const val PEDRO_VICENTE_URL = "https://pedrovicente.pt"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = koinViewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onScreenEntered()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.section_app_preferences),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettingsItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        title = stringResource(R.string.settings_language_title),
                        subtitle = stringResource(R.string.settings_language_subtitle),
                        onClick = {
                            val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                            context.startActivity(intent)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.section_about),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.settings_version),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.settings_about_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.section_reset_cards),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ResetCardsSection(
                        status = uiState.resetCardsStatus,
                        onResetCardsClicked = { showResetConfirmDialog = true }
                    )
                }

                if (uiState.updatesEnabled) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.section_updates),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        UpdateSection(
                            status = uiState.updateStatus,
                            onUpdateClicked = viewModel::onUpdateClicked,
                            onEnableSideloadingClicked = viewModel::onEnableSideloadingClicked
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                LoopGainFooter(modifier = Modifier.fillMaxWidth())
            }
        }
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text(stringResource(R.string.reset_dialog_title)) },
            text = { Text(stringResource(R.string.reset_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetConfirmDialog = false
                        viewModel.onResetCardsClicked()
                    }
                ) {
                    Text(stringResource(R.string.reset_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }
}

/**
 * Info text + button that makes every card hidden via swipe-down on Home visible again (see
 * [SettingsViewModel.onResetCardsClicked]). The actual reset only runs after the caller confirms
 * in a dialog, since it's a bulk, hard-to-undo action.
 */
@Composable
private fun ResetCardsSection(status: ResetCardsStatus, onResetCardsClicked: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(R.string.reset_cards_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onResetCardsClicked,
            enabled = status != ResetCardsStatus.Resetting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.reset_cards_button))
        }

        when (status) {
            is ResetCardsStatus.Idle -> Unit
            is ResetCardsStatus.Resetting -> UpdateStatusRow(text = stringResource(R.string.resetting_cards))
            is ResetCardsStatus.Done ->
                Text(
                    text = stringResource(R.string.reset_cards_done),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
        }
    }
}

/**
 * Info text + button that checks GitHub Releases and, if allowed, downloads and installs a newer
 * build (see [SettingsViewModel.onUpdateClicked]). The button stays enabled in every state except
 * while a check/download is actually in flight, so [UpdateStatus.SideloadingBlocked]/
 * [UpdateStatus.Failed]/[UpdateStatus.UpToDate] can all be retried with a plain second tap - e.g.
 * after the user enables sideloading in system Settings and returns to this screen.
 */
@Composable
private fun UpdateSection(status: UpdateStatus, onUpdateClicked: () -> Unit, onEnableSideloadingClicked: () -> Unit) {
    val isBusy = status is UpdateStatus.Checking || status is UpdateStatus.Downloading

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(R.string.update_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onUpdateClicked,
            enabled = !isBusy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.update_button))
        }

        when (status) {
            is UpdateStatus.Idle -> Unit
            is UpdateStatus.Checking -> UpdateStatusRow(text = stringResource(R.string.checking_updates))
            is UpdateStatus.Downloading -> UpdateStatusRow(text = stringResource(R.string.downloading_update))
            is UpdateStatus.UpToDate ->
                Text(
                    text = stringResource(R.string.up_to_date_format, status.currentVersionName),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            is UpdateStatus.UpdateAvailable -> Unit
            is UpdateStatus.Failed ->
                Text(
                    text = status.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            is UpdateStatus.SideloadingBlocked -> {
                Text(
                    text = stringResource(R.string.sideloading_blocked_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onEnableSideloadingClicked,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.enable_installing_updates))
                }
            }
        }
    }
}

@Composable
private fun UpdateStatusRow(text: String) {
    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

/**
 * Attribution line closing the screen: the app's place in the wider LoopGain tool family, with the
 * brand and the author each linking out to their own site.
 */
@Composable
private fun LoopGainFooter(modifier: Modifier = Modifier) {
    val loopGain = stringResource(R.string.loopgain_name)
    val author = stringResource(R.string.pedro_vicente_name)
    val footer = stringResource(R.string.settings_loopgain_footer, loopGain, author)
    val linkStyles = TextLinkStyles(
        style = SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline
        )
    )

    val annotatedFooter = remember(footer, loopGain, author, linkStyles) {
        buildAnnotatedString {
            append(footer)
            addUrlLink(footer, loopGain, LOOPGAIN_URL, linkStyles)
            addUrlLink(footer, author, PEDRO_VICENTE_URL, linkStyles)
        }
    }

    Text(
        text = annotatedFooter,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

/**
 * Links the first occurrence of [label] in [text] to [url]. A translation that drops or rewrites
 * the placeholder simply keeps that part as plain text instead of blowing up on a bad range.
 */
private fun AnnotatedString.Builder.addUrlLink(text: String, label: String, url: String, styles: TextLinkStyles) {
    val start = text.indexOf(label)
    if (start < 0) return
    addLink(LinkAnnotation.Url(url, styles), start, start + label.length)
}

@Composable
private fun SettingsItem(icon: @Composable () -> Unit, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
