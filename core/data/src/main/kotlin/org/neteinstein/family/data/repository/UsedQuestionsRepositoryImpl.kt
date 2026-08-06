package org.neteinstein.family.data.repository

import org.neteinstein.family.data.local.CardDao
import org.neteinstein.family.domain.repository.UsedQuestionsRepository

class UsedQuestionsRepositoryImpl(private val cardDao: CardDao) : UsedQuestionsRepository {

    override suspend fun getUsedQuestionIds(): Set<Int> = cardDao.getHiddenIds().toSet()

    override suspend fun markAsUsed(questionId: Int) {
        cardDao.markHidden(questionId)
    }

    override suspend fun resetUsedQuestions() {
        cardDao.resetAllHidden()
    }
}
