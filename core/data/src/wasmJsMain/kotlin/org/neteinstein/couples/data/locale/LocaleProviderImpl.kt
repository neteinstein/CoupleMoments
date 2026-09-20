package org.neteinstein.couples.data.locale

import org.neteinstein.couples.domain.repository.LocaleProvider

/**
 * `navigator.language` (e.g. "en-US", "pt-BR") is the browser's own reported UI language - the
 * closest wasmJs equivalent to Android's `Locale.getDefault()`/iOS's `NSLocale.currentLocale`.
 * Read through an inline `js("...")` snippet (Kotlin/Wasm JS interop) rather than the
 * `kotlinx-browser` library, since one global property read doesn't warrant a whole dependency.
 *
 * The region subtag is dropped: this app keys its content on bare ISO 639-1 codes.
 */
class LocaleProviderImpl : LocaleProvider {
    override fun currentLanguageCode(): String = browserLanguage().substringBefore('-')
}

private fun browserLanguage(): String = js("navigator.language")
