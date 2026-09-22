package org.neteinstein.couples.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.neteinstein.couples.MainViewModel
import org.neteinstein.couples.data.di.dataModule
import org.neteinstein.couples.feature.game.di.gameModule
import org.neteinstein.couples.feature.home.di.homeModule
import org.neteinstein.couples.feature.settings.di.settingsModule

/**
 * [updatesEnabled] is passed in rather than read from `BuildConfig` here: this module is
 * commonMain code with no Android build variant behind it. androidApp passes its own
 * `BuildConfig.UPDATES_ENABLED` (true for the "github" direct-APK flavor that self-updates from
 * GitHub Releases, false for "playstore", which the Play Store itself updates); iOS and Web always
 * pass false, since neither can install another build of itself - see `feature:settings`, which
 * hides the whole "Updates" section when this is false.
 */
fun appModule(updatesEnabled: Boolean): Module =
    module {
        single(named("updatesEnabled")) { updatesEnabled }

        viewModel { MainViewModel(get(), get(), get()) }

        includes(dataModule, homeModule, gameModule, settingsModule)
    }
