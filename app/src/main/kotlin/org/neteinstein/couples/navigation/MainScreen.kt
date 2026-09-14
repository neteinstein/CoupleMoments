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
import androidx.compose.ui.res.stringResource
import org.neteinstein.couples.R
import org.neteinstein.couples.feature.game.GameScreen
import org.neteinstein.couples.feature.home.HomeScreen

private enum class MainTab {
    Questions,
    Game,
}

@Composable
fun MainScreen(onSettingsClick: () -> Unit) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Questions) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == MainTab.Questions,
                    onClick = { selectedTab = MainTab.Questions },
                    icon = { Icon(imageVector = Icons.Default.QuestionAnswer, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_questions)) },
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.Game,
                    onClick = { selectedTab = MainTab.Game },
                    icon = { Icon(imageVector = Icons.Default.SportsEsports, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_game)) },
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
