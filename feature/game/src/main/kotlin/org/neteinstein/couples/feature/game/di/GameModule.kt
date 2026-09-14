package org.neteinstein.couples.feature.game.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.neteinstein.couples.feature.game.GameViewModel

val gameModule =
    module {
        viewModel { GameViewModel() }
    }
