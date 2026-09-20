package org.neteinstein.couples.data.locale

import org.neteinstein.couples.domain.repository.LocaleProvider
import java.util.Locale

/**
 * `Locale.getDefault()` is the process-wide default locale, which the OS itself overrides to match
 * the user's per-app language selection (from `android:localeConfig`) for as long as this app's
 * process is alive - no `Context`/`LocaleManager` lookup needed.
 */
class LocaleProviderImpl : LocaleProvider {
    override fun currentLanguageCode(): String = Locale.getDefault().language
}
