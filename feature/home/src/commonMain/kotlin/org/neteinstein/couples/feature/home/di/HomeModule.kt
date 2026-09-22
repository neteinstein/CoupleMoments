package org.neteinstein.couples.feature.home.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.neteinstein.couples.feature.home.HomeViewModel

val homeModule =
    module {
        viewModel { HomeViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    }
