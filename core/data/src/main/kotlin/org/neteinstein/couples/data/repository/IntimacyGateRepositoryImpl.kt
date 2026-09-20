package org.neteinstein.couples.data.repository

import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.neteinstein.couples.domain.repository.IntimacyGateRepository

/**
 * [IntimacyGateRepository] backed by a single [com.russhwolf.settings.Settings] boolean entry - no
 * Room table needed for one flag that isn't queried, filtered, or joined against anything.
 *
 * [settings] is expected to be bound (see `DataModule.kt`) to the exact same on-disk store the
 * previous `context.getSharedPreferences("intimacy_gate", MODE_PRIVATE)` used (via
 * `SharedPreferencesSettings`), so upgrading installs keep their saved acknowledgement.
 */
class IntimacyGateRepositoryImpl(
    private val settings: Settings,
) : IntimacyGateRepository {
    override suspend fun hasAcknowledged(): Boolean =
        withContext(Dispatchers.IO) {
            settings.getBoolean(KEY_ACKNOWLEDGED, false)
        }

    override suspend fun setAcknowledged() {
        withContext(Dispatchers.IO) {
            settings.putBoolean(KEY_ACKNOWLEDGED, true)
        }
    }

    private companion object {
        const val KEY_ACKNOWLEDGED = "acknowledged"
    }
}
