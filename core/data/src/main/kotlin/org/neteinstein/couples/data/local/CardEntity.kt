package org.neteinstein.couples.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room row for a single conversation card. [id] reuses the pre-existing seed ids from
 * [org.neteinstein.couples.data.source.QuestionSeedData], which are already namespaced per
 * language (en=1-120, pt=201-320, es=401-520, fr=601-720, de=801-920), so a single table keyed on
 * [id] never collides across languages - no need for a separate table per language.
 */
@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey val id: Int,
    val text: String,
    val languageCode: String,
    val category: String,
    val isHidden: Boolean = false,
)
