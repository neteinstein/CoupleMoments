@file:OptIn(ExperimentalWasmJsInterop::class)

package org.neteinstein.couples.data.local

import com.russhwolf.settings.Settings
import kotlinx.coroutines.await

/**
 * [CardDao] for the web build, backed by IndexedDB through [IndexedDbDatabase] - the browser's
 * stand-in for the SQLDelight database Android and iOS use (SQLDelight publishes no wasmJs
 * driver). Unlike the original `localStorage`-only [WebCardDao], the full card rows persist across
 * page loads, so [org.neteinstein.couples.data.repository.QuestionRepositoryImpl] re-seeds only
 * when [org.neteinstein.couples.data.source.QuestionSeedData.VERSION] actually changes, exactly as
 * on the other two targets.
 *
 * When IndexedDB can't be opened, every call is delegated to [fallback] instead.
 *
 * Hidden ids written by the earlier `localStorage`-backed build (under [WebCardDao]'s key in
 * [legacySettings]) are folded into the first [insertAll] - which the repository always reaches on
 * a fresh database, since [IndexedDbSeedMetadataDao] reports no stored version - and then removed,
 * so a user upgrading from that build keeps the cards they had already swiped away.
 */
class IndexedDbCardDao(
    private val database: IndexedDbDatabase,
    private val legacySettings: Settings,
    private val fallback: CardDao,
) : CardDao {
    override suspend fun insertAll(cards: List<CardEntity>) {
        val db = database.get() ?: return fallback.insertAll(cards)
        val legacyHidden = WebCardDao.readHiddenIds(legacySettings)
        val rows =
            cards.map { card ->
                idbCard(
                    id = card.id,
                    text = card.text,
                    languageCode = card.languageCode,
                    category = card.category,
                    isHidden = card.isHidden || card.id in legacyHidden,
                    audience = card.audience,
                )
            }
        idbPutAll(db, CARDS_STORE, rows.toJsArray()).await<JsAny?>()
        if (legacyHidden.isNotEmpty()) WebCardDao.clearHiddenIds(legacySettings)
    }

    override suspend fun deleteAll() {
        val db = database.get() ?: return fallback.deleteAll()
        idbClear(db, CARDS_STORE).await<JsAny?>()
    }

    override suspend fun getCardsForLanguage(languageCode: String): List<CardEntity> {
        val db = database.get() ?: return fallback.getCardsForLanguage(languageCode)
        val rows = idbCardsForLanguage(db, languageCode).await<JsArray<JsAny>>()
        return List(rows.length) { index -> rows[index]!!.toCardEntity() }
    }

    override suspend fun getHiddenIds(): List<Int> {
        val db = database.get() ?: return fallback.getHiddenIds()
        val ids = idbHiddenCardIds(db).await<JsArray<JsNumber>>()
        return List(ids.length) { index -> ids[index]!!.toInt() }
    }

    override suspend fun markHidden(cardId: Int) {
        val db = database.get() ?: return fallback.markHidden(cardId)
        idbMarkHidden(db, cardId).await<JsAny?>()
    }

    override suspend fun resetAllHidden() {
        val db = database.get() ?: return fallback.resetAllHidden()
        idbResetAllHidden(db).await<JsAny?>()
    }

    private fun JsAny.toCardEntity() =
        CardEntity(
            id = idbCardId(this),
            text = idbCardText(this),
            languageCode = idbCardLanguageCode(this),
            category = idbCardCategory(this),
            isHidden = idbCardIsHidden(this),
            audience = idbCardAudience(this),
        )

    private companion object {
        const val CARDS_STORE = "cards"
    }
}

/**
 * [SeedMetadataDao] for the web build, persisted in IndexedDB's `seed_metadata` store alongside
 * [IndexedDbCardDao]'s cards. Delegates to [fallback] (which never reports a version, forcing a
 * re-seed of the in-memory cards each page load) when IndexedDB can't be opened - both DAOs share
 * one [IndexedDbDatabase], so they always fall back together.
 */
class IndexedDbSeedMetadataDao(
    private val database: IndexedDbDatabase,
    private val fallback: SeedMetadataDao,
) : SeedMetadataDao {
    override suspend fun getVersion(): Int? {
        val db = database.get() ?: return fallback.getVersion()
        return idbSeedVersion(db, SeedMetadataEntity.SINGLETON_ID).await<JsNumber?>()?.toInt()
    }

    override suspend fun setVersion(metadata: SeedMetadataEntity) {
        val db = database.get() ?: return fallback.setVersion(metadata)
        val row = idbSeedMetadata(metadata.id, metadata.version)
        idbPutAll(db, SEED_METADATA_STORE, listOf(row).toJsArray()).await<JsAny?>()
    }

    private companion object {
        const val SEED_METADATA_STORE = "seed_metadata"
    }
}
