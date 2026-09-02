package org.neteinstein.family.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [CardEntity::class, SeedMetadataEntity::class], version = 2, exportSchema = false)
abstract class FamilyMomentsDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao

    abstract fun seedMetadataDao(): SeedMetadataDao

    companion object {
        /**
         * Adds [SeedMetadataEntity]'s table only - [CardEntity]'s `cards` table (and its
         * `isHidden` state) is left untouched, unlike a destructive fallback migration would.
         * A device migrating from version 1 has no `seed_metadata` row yet, so
         * [org.neteinstein.family.data.repository.QuestionRepositoryImpl] naturally treats it as
         * seed-version-mismatched on the next launch and reseeds once, the same as a fresh
         * install would.
         */
        val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `seed_metadata` (`id` INTEGER NOT NULL, `version` INTEGER NOT NULL, PRIMARY KEY(`id`))",
                    )
                }
            }
    }
}
