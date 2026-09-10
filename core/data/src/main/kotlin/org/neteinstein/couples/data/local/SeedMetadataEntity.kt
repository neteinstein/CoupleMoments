package org.neteinstein.couples.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table (fixed [id]) tracking which
 * [org.neteinstein.couples.data.source.QuestionSeedData.VERSION] is currently applied to
 * [CardEntity], so [org.neteinstein.couples.data.repository.QuestionRepositoryImpl] knows when to
 * fully replace seeded cards instead of only ever being able to add missing ones.
 */
@Entity(tableName = "seed_metadata")
data class SeedMetadataEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val version: Int,
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}
