package org.neteinstein.couples.data.local

import com.russhwolf.settings.Settings

/**
 * [CardDao] for the web build. SQLDelight publishes no wasmJs driver, so there is no SQL engine to
 * put behind the generated queries on this target - but [CardDao] was deliberately kept
 * persistence-agnostic (see its doc comment), so a different backing store slots in without any
 * repository above it noticing.
 *
 * The card rows themselves are held in memory, which costs nothing:
 * [org.neteinstein.couples.data.repository.QuestionRepositoryImpl] re-seeds them from
 * [org.neteinstein.couples.data.source.QuestionSeedData] - a compiled-in constant - on every
 * launch anyway, because [WebSeedMetadataDao] always reports "no version stored". The one piece of
 * genuinely user-owned state is which cards have been hidden, so that alone is persisted through
 * [Settings] (i.e. the browser's `localStorage`) and survives a page reload.
 */
class WebCardDao(
    private val settings: Settings,
) : CardDao {
    private var cards: List<CardEntity> = emptyList()

    override suspend fun insertAll(cards: List<CardEntity>) {
        val hidden = hiddenIds()
        this.cards = cards.map { it.copy(isHidden = it.id in hidden) }
    }

    override suspend fun deleteAll() {
        cards = emptyList()
    }

    override suspend fun getCardsForLanguage(languageCode: String): List<CardEntity> = cards.filter { it.languageCode == languageCode }

    override suspend fun getHiddenIds(): List<Int> = hiddenIds().toList()

    override suspend fun markHidden(cardId: Int) {
        writeHiddenIds(hiddenIds() + cardId)
        cards = cards.map { if (it.id == cardId) it.copy(isHidden = true) else it }
    }

    override suspend fun resetAllHidden() {
        writeHiddenIds(emptySet())
        cards = cards.map { it.copy(isHidden = false) }
    }

    private fun hiddenIds(): Set<Int> =
        settings
            .getStringOrNull(HIDDEN_IDS_KEY)
            ?.split(',')
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?.toSet()
            .orEmpty()

    private fun writeHiddenIds(ids: Set<Int>) {
        settings.putString(HIDDEN_IDS_KEY, ids.joinToString(separator = ","))
    }

    private companion object {
        const val HIDDEN_IDS_KEY = "hidden_card_ids"
    }
}

/**
 * [SeedMetadataDao] for the web build. Deliberately never reports a stored version, so
 * [org.neteinstein.couples.data.repository.QuestionRepositoryImpl] re-seeds the in-memory card
 * list from [org.neteinstein.couples.data.source.QuestionSeedData] once per page load - which is
 * exactly what [WebCardDao] needs, since it starts empty every time. Writes are accepted and
 * dropped rather than rejected, so the repository's normal seeding path works unchanged.
 */
class WebSeedMetadataDao : SeedMetadataDao {
    override suspend fun getVersion(): Int? = null

    override suspend fun setVersion(metadata: SeedMetadataEntity) = Unit
}
