package org.neteinstein.couples.data.local

/**
 * Persistence-framework-agnostic contract for [CardEntity] storage - [CardDaoImpl] is the
 * SQLDelight-backed implementation. Kept as a separate interface (rather than folding
 * [CardDaoImpl] directly into [org.neteinstein.couples.data.repository.QuestionRepositoryImpl]/
 * [org.neteinstein.couples.data.repository.UsedQuestionsRepositoryImpl]) so those repositories -
 * and their tests - stay unaware of the storage mechanism underneath.
 */
interface CardDao {
    suspend fun insertAll(cards: List<CardEntity>)

    suspend fun deleteAll()

    suspend fun getCardsForLanguage(languageCode: String): List<CardEntity>

    suspend fun getHiddenIds(): List<Int>

    suspend fun markHidden(cardId: Int)

    suspend fun resetAllHidden()
}
