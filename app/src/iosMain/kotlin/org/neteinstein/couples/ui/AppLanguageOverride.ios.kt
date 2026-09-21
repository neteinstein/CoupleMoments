package org.neteinstein.couples.ui

/**
 * No-op for now: `NSLocale.currentLocale`, which Compose Resources reads on iOS, is fixed for the
 * lifetime of the process - writing `AppleLanguages` into `NSUserDefaults` only takes effect on
 * the next launch, so it would leave the UI strings and the picker disagreeing for the rest of
 * the session. The override still switches the question content (see
 * [org.neteinstein.couples.domain.usecase.GetContentLanguageUseCase]); UI strings keep following
 * the device language.
 */
actual fun applyAppLanguageOverride(languageCode: String?) = Unit
