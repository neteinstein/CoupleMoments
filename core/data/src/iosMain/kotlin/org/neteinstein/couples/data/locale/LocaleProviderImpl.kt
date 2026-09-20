package org.neteinstein.couples.data.locale

import org.neteinstein.couples.domain.repository.LocaleProvider
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

/**
 * `NSLocale.currentLocale.languageCode` is the closest iOS equivalent to Android's
 * `Locale.getDefault().language`. iOS has no per-app language override this app can drive, so the
 * in-app picker (see `GetContentLanguageUseCase`) takes precedence over this when the user sets one.
 */
class LocaleProviderImpl : LocaleProvider {
    override fun currentLanguageCode(): String = NSLocale.currentLocale.languageCode
}
