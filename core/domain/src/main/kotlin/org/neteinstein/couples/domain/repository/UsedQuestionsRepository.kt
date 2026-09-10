package org.neteinstein.couples.domain.repository

interface UsedQuestionsRepository {
    suspend fun getUsedQuestionIds(): Set<Int>

    suspend fun markAsUsed(questionId: Int)

    /** Makes every previously-hidden card visible again. */
    suspend fun resetUsedQuestions()
}
