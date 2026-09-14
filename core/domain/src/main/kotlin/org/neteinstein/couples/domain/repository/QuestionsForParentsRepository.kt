package org.neteinstein.couples.domain.repository

/**
 * Whether the Settings > "Couple Questions For Parents" toggle is on - i.e. whether
 * [org.neteinstein.couples.domain.model.QuestionAudience.WithKids] questions should be included
 * alongside [org.neteinstein.couples.domain.model.QuestionAudience.WithoutKids] and
 * [org.neteinstein.couples.domain.model.QuestionAudience.Both] ones. Off by default.
 */
interface QuestionsForParentsRepository {
    suspend fun isEnabled(): Boolean

    suspend fun setEnabled(enabled: Boolean)
}
