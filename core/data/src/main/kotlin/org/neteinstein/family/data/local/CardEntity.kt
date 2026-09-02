package org.neteinstein.family.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room row for a single conversation card. [id] reuses the pre-existing seed ids from
 * [org.neteinstein.family.data.source.QuestionSeedData], which are already namespaced per
 * language (en=1-50, pt=101-150, es=201-250, fr=301-350, de=401-450), so a single table keyed on
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
