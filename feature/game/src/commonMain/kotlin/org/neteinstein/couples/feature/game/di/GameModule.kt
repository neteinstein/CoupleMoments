package org.neteinstein.couples.feature.game.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.neteinstein.couples.feature.game.GameViewModel

val gameModule =
    module {
        viewModel { GameViewModel(get(), get()) }
    }
