package org.neteinstein.family.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CardEntity::class], version = 1, exportSchema = false)
abstract class FamilyMomentsDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
}
