package org.neteinstein.couples.data.repository

import com.russhwolf.settings.Settings
import kotlinx.coroutines.withContext
import org.neteinstein.couples.data.local.ioDispatcher
import org.neteinstein.couples.domain.repository.QuestionsForParentsRepository

/**
 * [QuestionsForParentsRepository] backed by a single [com.russhwolf.settings.Settings] boolean
 * entry - no Room table needed for one flag that isn't queried, filtered, or joined against
 * anything.
 *
 * [settings] is expected to be bound (see `DataModule.kt`) to the exact same on-disk store the
 * previous `context.getSharedPreferences("questions_for_parents", MODE_PRIVATE)` used (via
 * `SharedPreferencesSettings`), so upgrading installs keep their saved toggle.
 */
class QuestionsForParentsRepositoryImpl(
    private val settings: Settings,
) : QuestionsForParentsRepository {
    override suspend fun isEnabled(): Boolean =
        withContext(ioDispatcher) {
            settings.getBoolean(KEY_ENABLED, false)
        }

    override suspend fun setEnabled(enabled: Boolean) {
        withContext(ioDispatcher) {
            settings.putBoolean(KEY_ENABLED, enabled)
        }
    }

    private companion object {
        const val KEY_ENABLED = "enabled"
    }
}
