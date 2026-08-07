package org.neteinstein.family.data.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.neteinstein.family.data.local.CardDao
import org.neteinstein.family.data.local.toDomain
import org.neteinstein.family.data.local.toEntity
import org.neteinstein.family.data.source.QuestionSeedData
import org.neteinstein.family.domain.model.Question
import org.neteinstein.family.domain.repository.QuestionRepository

class QuestionRepositoryImpl(private val cardDao: CardDao) : QuestionRepository {

    private val seedMutex = Mutex()
    private var isSeeded = false

    override suspend fun getQuestions(languageCode: String): List<Question> {
        ensureSeeded()
        return cardDao.getCardsForLanguage(languageCode).map { it.toDomain() }
    }

    override suspend fun getRandomQuestion(languageCode: String): Question? =
        getQuestions(languageCode).randomOrNull()

    /**
     * Populates the database from [QuestionSeedData] once per process lifetime. Gates on the
     * table holding fewer rows than the current seed data - not on count being zero - so cards
     * added to [QuestionSeedData] after a device was first seeded still get inserted (via
     * [CardDao.insertAll]'s conflict-ignore strategy, which no-ops the rows that already exist)
     * instead of being silently skipped forever, while a device that's already fully seeded skips
     * the insert call entirely instead of re-attempting it on every cold start.
     */
    private suspend fun ensureSeeded() {
        if (isSeeded) return
        seedMutex.withLock {
            if (isSeeded) return
            if (cardDao.count() < QuestionSeedData.all.size) {
                cardDao.insertAll(QuestionSeedData.all.map { it.toEntity() })
            }
            isSeeded = true
        }
    }
}
