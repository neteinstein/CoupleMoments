package org.neteinstein.couples.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * [CardDao] backed by SQLDelight's generated `CardQueries` (from `Card.sq`), replacing the
 * previous Room `@Dao`. Every query is dispatched onto [Dispatchers.IO] here, the same way Room's
 * suspend DAO methods did internally - SQLDelight's Android driver calls are synchronous/blocking.
 *
 * `selectForLanguage` maps rows directly into [CardEntity] via SQLDelight's per-query mapper
 * lambda overload rather than going through the generated row type (which SQLDelight would name
 * `Cards`, after the table) - this keeps that generated type as a pure implementation detail
 * nothing outside this file needs to know about.
 */
class CardDaoImpl(
    private val queries: CardQueries,
) : CardDao {
    override suspend fun insertAll(cards: List<CardEntity>) =
        withContext(Dispatchers.IO) {
            queries.transaction {
                cards.forEach { card ->
                    queries.insertOrIgnore(
                        id = card.id.toLong(),
                        text = card.text,
                        languageCode = card.languageCode,
                        category = card.category,
                        isHidden = card.isHidden,
                        audience = card.audience,
                    )
                }
            }
        }

    override suspend fun deleteAll() =
        withContext(Dispatchers.IO) {
            queries.deleteAll()
        }

    override suspend fun getCardsForLanguage(languageCode: String): List<CardEntity> =
        withContext(Dispatchers.IO) {
            queries.selectForLanguage(languageCode) { id, text, cardLanguageCode, category, isHidden, audience ->
                CardEntity(
                    id = id.toInt(),
                    text = text,
                    languageCode = cardLanguageCode,
                    category = category,
                    isHidden = isHidden,
                    audience = audience,
                )
            }.executeAsList()
        }

    override suspend fun getHiddenIds(): List<Int> =
        withContext(Dispatchers.IO) {
            queries.selectHiddenIds().executeAsList().map { it.toInt() }
        }

    override suspend fun markHidden(cardId: Int) =
        withContext(Dispatchers.IO) {
            queries.markHidden(cardId.toLong())
        }

    override suspend fun resetAllHidden() =
        withContext(Dispatchers.IO) {
            queries.resetAllHidden()
        }
}
