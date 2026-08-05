package org.neteinstein.family.feature.settings.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.neteinstein.family.feature.settings.SettingsViewModel

val settingsModule = module {
    viewModel { SettingsViewModel(get(), get(), get()) }
}
