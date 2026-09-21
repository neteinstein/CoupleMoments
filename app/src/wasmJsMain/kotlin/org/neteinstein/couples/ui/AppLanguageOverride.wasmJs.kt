package org.neteinstein.couples.ui

/**
 * Compose Resources resolves `values-xx` from `Locale.current`, which on wasmJs reads
 * `window.navigator.languages` on every access (compose-multiplatform's
 * `ui-text/.../intl/Actuals.wasm.kt`). The browser exposes no way to change the reported
 * languages, so the only place an in-app override can be injected is that property itself: an own
 * accessor defined on the `navigator` instance shadows the `Navigator.prototype` getter for every
 * later read, ours and Compose's alike.
 *
 * The browser's real list is captured on the first call and restored whenever [languageCode] is
 * `null` ("Automatic"), so switching back keeps the full tags (`pt-BR`, not `pt`) the browser
 * reported - `LocaleProviderImpl`, which reads `navigator.language`, is deliberately left alone
 * and keeps reporting the browser's own language.
 */
actual fun applyAppLanguageOverride(languageCode: String?) {
    overrideNavigatorLanguages(languageCode ?: "")
}

private fun overrideNavigatorLanguages(languageCode: String): Unit =
    js(
        """{
            var navigator = window.navigator;
            if (!navigator.__originalLanguages) {
                navigator.__originalLanguages = Array.prototype.slice.call(navigator.languages);
            }
            var languages = languageCode ? [languageCode] : navigator.__originalLanguages;
            Object.defineProperty(navigator, 'languages', {
                get: function () { return languages; },
                configurable: true
            });
        }""",
    )
