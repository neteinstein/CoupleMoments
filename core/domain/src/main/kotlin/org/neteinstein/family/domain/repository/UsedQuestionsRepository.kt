package org.neteinstein.family.domain.repository

interface UsedQuestionsRepository {
    suspend fun getUsedQuestionIds(): Set<Int>
    suspend fun markAsUsed(questionId: Int)
}
