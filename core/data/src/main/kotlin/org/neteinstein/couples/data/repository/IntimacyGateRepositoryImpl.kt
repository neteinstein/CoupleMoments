package org.neteinstein.couples.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.neteinstein.couples.domain.repository.IntimacyGateRepository

/**
 * [IntimacyGateRepository] backed by a single [android.content.SharedPreferences] boolean - no
 * Room table needed for one flag that isn't queried, filtered, or joined against anything.
 */
class IntimacyGateRepositoryImpl(
    private val context: Context,
) : IntimacyGateRepository {
    private val prefs by lazy { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    override suspend fun hasAcknowledged(): Boolean =
        withContext(Dispatchers.IO) {
            prefs.getBoolean(KEY_ACKNOWLEDGED, false)
        }

    override suspend fun setAcknowledged() {
        withContext(Dispatchers.IO) {
            prefs.edit().putBoolean(KEY_ACKNOWLEDGED, true).apply()
        }
    }

    private companion object {
        const val PREFS_NAME = "intimacy_gate"
        const val KEY_ACKNOWLEDGED = "acknowledged"
    }
}
