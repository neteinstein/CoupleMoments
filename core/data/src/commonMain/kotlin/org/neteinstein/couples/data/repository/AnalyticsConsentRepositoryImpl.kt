package org.neteinstein.couples.data.repository

import com.russhwolf.settings.Settings
import kotlinx.coroutines.withContext
import org.neteinstein.couples.data.local.ioDispatcher
import org.neteinstein.couples.domain.repository.AnalyticsConsentRepository

/**
 * [AnalyticsConsentRepository] backed by a single [Settings] boolean, mirroring
 * [QuestionsForParentsRepositoryImpl]'s shape - one flag that is never queried or joined against
 * anything needs no table of its own.
 *
 * Defaults to `true` (opted in) when nothing is stored. That is a deliberate product choice
 * rather than a technical one: the Settings > Privacy toggle and PRIVACY.md disclose the
 * collection, and everything reported is anonymous and low-cardinality. Flipping the default to
 * opt-*in* is a one-word change here - see the PR description.
 */
class AnalyticsConsentRepositoryImpl(
    private val settings: Settings,
) : AnalyticsConsentRepository {
    override suspend fun isEnabled(): Boolean =
        withContext(ioDispatcher) {
            settings.getBoolean(KEY_ENABLED, true)
        }

    override suspend fun setEnabled(enabled: Boolean) {
        withContext(ioDispatcher) {
            settings.putBoolean(KEY_ENABLED, enabled)
        }
    }

    private companion object {
        /**
         * Prefixed rather than a bare "enabled" like [QuestionsForParentsRepositoryImpl]'s: on
         * Web every named `Settings` qualifier resolves to the same `localStorage` store (see
         * `PlatformDataModule.wasmJs.kt`), so a second "enabled" key would silently be the same
         * entry as the parents-mode one. It is also the key `firebase-init.js` reads directly, to
         * decide whether to initialize Analytics at all on an opted-out page load.
         */
        const val KEY_ENABLED = "analytics_enabled"
    }
}
