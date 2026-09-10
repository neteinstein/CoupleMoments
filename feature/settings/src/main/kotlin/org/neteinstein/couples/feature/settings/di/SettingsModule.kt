package org.neteinstein.couples.feature.settings.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.neteinstein.couples.feature.settings.SettingsViewModel

val settingsModule =
    module {
        viewModel { SettingsViewModel(get(), get(), get(), get(), get(named("updatesEnabled"))) }
    }
