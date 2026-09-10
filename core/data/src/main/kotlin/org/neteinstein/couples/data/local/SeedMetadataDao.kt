package org.neteinstein.couples.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SeedMetadataDao {
    @Query("SELECT version FROM seed_metadata LIMIT 1")
    suspend fun getVersion(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setVersion(metadata: SeedMetadataEntity)
}
