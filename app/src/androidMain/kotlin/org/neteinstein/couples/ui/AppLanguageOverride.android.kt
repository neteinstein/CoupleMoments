package org.neteinstein.couples.ui

/**
 * No-op: Android never sets an override. Its language row deep-links into the system's per-app
 * language settings (see `feature:settings`'s `rememberOpenLanguageSettingsAction`), and the OS
 * then applies the choice to the process locale itself - which is exactly what Compose Resources
 * reads.
 */
actual fun applyAppLanguageOverride(languageCode: String?) = Unit
