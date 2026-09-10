package org.neteinstein.couples.domain.repository

/**
 * Reads the language the OS currently has applied to this app's process - reflects the user's
 * per-app "App Language" selection in system Settings, not just the device-wide locale. Declared
 * here (rather than directly as a `java.util.Locale`-backed class in `feature:home`) so the
 * feature module can keep depending only on `core:domain`/`core:ui`, per this project's module
 * boundary rules - the real implementation lives in `core:data`.
 */
interface LocaleProvider {
    /** ISO 639-1 language code (e.g. "en", "pt"), no region subtag. */
    fun currentLanguageCode(): String
}
