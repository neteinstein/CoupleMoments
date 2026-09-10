package org.neteinstein.couples.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CardDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(cards: List<CardEntity>)

    @Query("DELETE FROM cards")
    suspend fun deleteAll()

    @Query("SELECT * FROM cards WHERE languageCode = :languageCode")
    suspend fun getCardsForLanguage(languageCode: String): List<CardEntity>

    @Query("SELECT id FROM cards WHERE isHidden = 1")
    suspend fun getHiddenIds(): List<Int>

    @Query("UPDATE cards SET isHidden = 1 WHERE id = :cardId")
    suspend fun markHidden(cardId: Int)

    @Query("UPDATE cards SET isHidden = 0")
    suspend fun resetAllHidden()
}
