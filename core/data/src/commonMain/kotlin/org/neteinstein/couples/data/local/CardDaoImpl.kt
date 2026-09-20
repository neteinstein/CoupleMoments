package org.neteinstein.couples.data.local

import kotlinx.coroutines.withContext

/**
 * [CardDao] backed by SQLDelight's generated `CardQueries` (from `Card.sq`), replacing the
 * previous Room `@Dao`. Every query is dispatched onto [Dispatchers.IO] here, the same way Room's
 * suspend DAO methods did internally - SQLDelight's Android driver calls are synchronous/blocking.
 *
 * `isHidden` is plain `INTEGER`/`Long` in the schema (see `Card.sq`'s header comment) - converted
 * to/from [Boolean] here at the DAO boundary so [CardEntity] keeps its existing shape.
 *
 * `selectForLanguage` maps rows directly into [CardEntity] via SQLDelight's per-query mapper
 * lambda overload rather than going through the generated row type (which SQLDelight would name
 * `Cards`, after the table) - this keeps that generated type as a pure implementation detail
 * nothing outside this file needs to know about.
 *
 * The four single-statement methods below (`deleteAll`/`markHidden`/`resetAllHidden`, and
 * [SeedMetadataDaoImpl.setVersion]) use a block body (`{ }`) rather than an expression body (`=`)
 * even though each is one line: SQLDelight's generated execute-style query functions return
 * `QueryResult<Long>` (affected-row count), and an expression body's return type is inferred from
 * its last expression - which would then conflict with [CardDao]'s `Unit`-returning signature. A
 * block body's implicit `Unit` return doesn't do that inference, so the query result is simply
 * discarded.
 */
class CardDaoImpl(
    private val queries: CardQueries,
) : CardDao {
    override suspend fun insertAll(cards: List<CardEntity>) =
        withContext(ioDispatcher) {
            queries.transaction {
                cards.forEach { card ->
                    queries.insertOrIgnore(
                        id = card.id.toLong(),
                        text = card.text,
                        languageCode = card.languageCode,
                        category = card.category,
                        isHidden = if (card.isHidden) 1L else 0L,
                        audience = card.audience,
                    )
                }
            }
        }

    override suspend fun deleteAll() {
        withContext(ioDispatcher) {
            queries.deleteAll()
        }
    }

    override suspend fun getCardsForLanguage(languageCode: String): List<CardEntity> =
        withContext(ioDispatcher) {
            val query =
                queries.selectForLanguage(languageCode) { id, text, cardLanguageCode, category, isHidden, audience ->
                    CardEntity(
                        id = id.toInt(),
                        text = text,
                        languageCode = cardLanguageCode,
                        category = category,
                        isHidden = isHidden != 0L,
                        audience = audience,
                    )
                }
            query.executeAsList()
        }

    override suspend fun getHiddenIds(): List<Int> =
        withContext(ioDispatcher) {
            queries.selectHiddenIds().executeAsList().map { it.toInt() }
        }

    override suspend fun markHidden(cardId: Int) {
        withContext(ioDispatcher) {
            queries.markHidden(cardId.toLong())
        }
    }

    override suspend fun resetAllHidden() {
        withContext(ioDispatcher) {
            queries.resetAllHidden()
        }
    }
}
