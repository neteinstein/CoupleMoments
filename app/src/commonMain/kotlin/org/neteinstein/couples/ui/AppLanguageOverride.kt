package org.neteinstein.couples.ui

/**
 * Applies the user's in-app language override ([org.neteinstein.couples.domain.model.AppLanguage]'s
 * bare ISO 639-1 [languageCode], or `null` for "follow the OS/browser") to whatever the *UI
 * strings* are resolved from, as opposed to the question content - which goes through
 * [org.neteinstein.couples.domain.usecase.GetContentLanguageUseCase] instead.
 *
 * Compose Resources has no public API for overriding its locale (`LocalComposeEnvironment` is
 * internal): it always resolves `values-xx` from the platform's own locale. So each platform has
 * to bend that platform locale, or do nothing when it already tracks the user's choice.
 */
expect fun applyAppLanguageOverride(languageCode: String?)
