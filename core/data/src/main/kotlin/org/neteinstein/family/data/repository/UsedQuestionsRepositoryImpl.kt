package org.neteinstein.family.data.repository

import android.content.Context
import org.neteinstein.family.domain.repository.UsedQuestionsRepository

class UsedQuestionsRepositoryImpl(context: Context) : UsedQuestionsRepository {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override suspend fun getUsedQuestionIds(): Set<Int> =
        prefs.getStringSet(KEY_USED_QUESTION_IDS, emptySet())
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            ?: emptySet()

    override suspend fun markAsUsed(questionId: Int) {
        val updated = getUsedQuestionIds() + questionId
        prefs.edit().putStringSet(KEY_USED_QUESTION_IDS, updated.map { it.toString() }.toSet()).apply()
    }

    private companion object {
        const val PREFS_NAME = "used_questions"
        const val KEY_USED_QUESTION_IDS = "used_question_ids"
    }
}
