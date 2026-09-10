package org.neteinstein.couples.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.neteinstein.couples.BuildConfig
import org.neteinstein.couples.data.di.dataModule
import org.neteinstein.couples.feature.home.di.homeModule
import org.neteinstein.couples.feature.settings.di.settingsModule

val appModule =
    module {
        // Flavor-gated: true for "github" (the direct-APK build that self-updates from GitHub
        // Releases), false for "playstore" (which the Play Store itself updates) - see
        // app/build.gradle.kts productFlavors and feature/settings's SettingsViewModel/SettingsScreen.
        single(named("updatesEnabled")) { BuildConfig.UPDATES_ENABLED }

        includes(dataModule, homeModule, settingsModule)
    }
