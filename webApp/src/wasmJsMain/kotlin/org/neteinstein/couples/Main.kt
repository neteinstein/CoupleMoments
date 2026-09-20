package org.neteinstein.couples

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import org.neteinstein.couples.di.doInitKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    doInitKoin()
    ComposeViewport {
        App()
    }
}
