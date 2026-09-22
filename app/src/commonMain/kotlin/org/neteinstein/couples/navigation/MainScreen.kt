package org.neteinstein.couples.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.neteinstein.couples.analytics.TrackScreenView
import org.neteinstein.couples.domain.analytics.AnalyticsEvent
import org.neteinstein.couples.domain.analytics.AnalyticsScreen
import org.neteinstein.couples.domain.analytics.AnalyticsTracker
import org.neteinstein.couples.feature.game.GameScreen
import org.neteinstein.couples.feature.home.HomeScreen
import org.neteinstein.couples.resources.Res
import org.neteinstein.couples.resources.tab_game
import org.neteinstein.couples.resources.tab_questions

private enum class MainTab(
    val analyticsScreen: AnalyticsScreen,
) {
    Questions(AnalyticsScreen.Questions),
    Game(AnalyticsScreen.Game),
}

@Composable
fun MainScreen(onSettingsClick: () -> Unit) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Questions) }
    val analyticsTracker: AnalyticsTracker = koinInject()

    // Reports the tab as a screen. Separate from the tab_selected event below, which the switch
    // itself emits: a screen view also happens on first entry and on return from Settings, where
    // no selection was made.
    TrackScreenView(selectedTab.analyticsScreen)

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == MainTab.Questions,
                    onClick = { selectedTab = selectTab(MainTab.Questions, analyticsTracker) },
                    icon = { Icon(imageVector = Icons.Default.QuestionAnswer, contentDescription = null) },
                    label = { Text(stringResource(Res.string.tab_questions)) },
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.Game,
                    onClick = { selectedTab = selectTab(MainTab.Game, analyticsTracker) },
                    icon = { Icon(imageVector = Icons.Default.SportsEsports, contentDescription = null) },
                    label = { Text(stringResource(Res.string.tab_game)) },
                )
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())) {
            when (selectedTab) {
                MainTab.Questions -> HomeScreen(onSettingsClick = onSettingsClick)
                MainTab.Game -> GameScreen(onSettingsClick = onSettingsClick)
            }
        }
    }
}

/**
 * Reports the switch and returns [tab], so the caller's state assignment stays a one-liner.
 * Fires on every tap, including a tap on the already-selected tab - that is a real (if idle)
 * interaction, and filtering it out here would hide it from the console entirely.
 */
private fun selectTab(
    tab: MainTab,
    analyticsTracker: AnalyticsTracker,
): MainTab {
    analyticsTracker.logEvent(AnalyticsEvent.TabSelected(tab.analyticsScreen))
    return tab
}
