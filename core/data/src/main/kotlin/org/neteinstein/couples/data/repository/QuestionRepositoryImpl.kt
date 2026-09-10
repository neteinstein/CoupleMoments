package org.neteinstein.couples.data.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.neteinstein.couples.data.local.CardDao
import org.neteinstein.couples.data.local.SeedMetadataDao
import org.neteinstein.couples.data.local.SeedMetadataEntity
import org.neteinstein.couples.data.local.toDomain
import org.neteinstein.couples.data.local.toEntity
import org.neteinstein.couples.data.source.QuestionSeedData
import org.neteinstein.couples.domain.model.Question
import org.neteinstein.couples.domain.repository.QuestionRepository

class QuestionRepositoryImpl(
    private val cardDao: CardDao,
    private val seedMetadataDao: SeedMetadataDao,
) : QuestionRepository {
    private val seedMutex = Mutex()
    private var isSeeded = false

    override suspend fun getQuestions(languageCode: String): List<Question> {
        ensureSeeded()
        return cardDao.getCardsForLanguage(languageCode).map { it.toDomain() }
    }

    override suspend fun getRandomQuestion(languageCode: String): Question? = getQuestions(languageCode).randomOrNull()

    /**
     * Fully replaces the `cards` table with [QuestionSeedData] whenever the stored seed version
     * doesn't match [QuestionSeedData.VERSION] - once per process lifetime otherwise. A full
     * delete-and-reinsert (rather than an additive, conflict-ignoring insert) is the only way a
     * question removed from [QuestionSeedData] actually disappears from a device seeded before it
     * was deleted; an additive insert can only ever add rows. Cards already marked hidden are
     * re-marked hidden after the replace (matched by id) so a version bump doesn't silently
     * un-hide every card a user has already swiped away.
     */
    private suspend fun ensureSeeded() {
        if (isSeeded) return
        seedMutex.withLock {
            if (isSeeded) return
            if (seedMetadataDao.getVersion() != QuestionSeedData.VERSION) {
                val hiddenIds = cardDao.getHiddenIds().toSet()
                cardDao.deleteAll()
                cardDao.insertAll(
                    QuestionSeedData.all.map { it.toEntity().copy(isHidden = it.id in hiddenIds) },
                )
                seedMetadataDao.setVersion(SeedMetadataEntity(version = QuestionSeedData.VERSION))
            }
            isSeeded = true
        }
    }
}
