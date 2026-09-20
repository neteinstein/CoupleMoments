package org.neteinstein.couples.data.local

/**
 * Single-row record (fixed [id]) tracking which
 * [org.neteinstein.couples.data.source.QuestionSeedData.VERSION] is currently applied to
 * [CardEntity], so [org.neteinstein.couples.data.repository.QuestionRepositoryImpl] knows when to
 * fully replace seeded cards instead of only ever being able to add missing ones.
 *
 * Plain data class - see [CardEntity]'s doc comment for why.
 */
data class SeedMetadataEntity(
    val id: Int = SINGLETON_ID,
    val version: Int,
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}
