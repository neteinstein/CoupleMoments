package org.neteinstein.couples.data.local

/**
 * A single conversation card row. [id] reuses the pre-existing seed ids from
 * [org.neteinstein.couples.data.source.QuestionSeedData], which are already namespaced per
 * language (en=1-120, pt=201-320, es=401-520, fr=601-720, de=801-920), so a single table keyed on
 * [id] never collides across languages - no need for a separate table per language.
 *
 * Plain data class (not a persistence-framework annotation target) - [CardDaoImpl] maps to/from
 * SQLDelight's generated row type at the query boundary, so this type stays framework-agnostic
 * for whichever platform's driver is behind it.
 */
data class CardEntity(
    val id: Int,
    val text: String,
    val languageCode: String,
    val category: String,
    val isHidden: Boolean = false,
    val audience: String = "both",
)
