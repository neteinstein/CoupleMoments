package org.neteinstein.couples.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.neteinstein.couples.domain.repository.QuestionsForParentsRepository

/**
 * [QuestionsForParentsRepository] backed by a single [android.content.SharedPreferences] boolean -
 * no Room table needed for one flag that isn't queried, filtered, or joined against anything.
 */
class QuestionsForParentsRepositoryImpl(
    private val context: Context,
) : QuestionsForParentsRepository {
    private val prefs by lazy { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    override suspend fun isEnabled(): Boolean =
        withContext(Dispatchers.IO) {
            prefs.getBoolean(KEY_ENABLED, false)
        }

    override suspend fun setEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
        }
    }

    private companion object {
        const val PREFS_NAME = "questions_for_parents"
        const val KEY_ENABLED = "enabled"
    }
}
