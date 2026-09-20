package org.neteinstein.couples.data.di

import org.koin.core.qualifier.named

/**
 * Each single-value preference gets its own named `Settings` store rather than one shared
 * instance. On Android that is load-bearing: these were three separate `SharedPreferences` files
 * before the KMP migration and must stay that way, or upgrading installs silently lose their
 * saved values. The other platforms keep the same split purely so the wiring reads identically
 * everywhere.
 *
 * Declared here, in commonMain, because [dataModule] resolves them and every target's
 * [platformDataModule] binds them - a qualifier defined in one of those and used in the other
 * would be a name only the compiler in one source set could see.
 */
val themeModeSettings = named("themeModeSettings")
val intimacyGateSettings = named("intimacyGateSettings")
val questionsForParentsSettings = named("questionsForParentsSettings")
val languageSettings = named("languageSettings")
