package org.neteinstein.couples.data.local

import org.neteinstein.couples.domain.model.Question
import org.neteinstein.couples.domain.model.QuestionCategory

private fun QuestionCategory.toStorageKey(): String =
    when (this) {
        QuestionCategory.IceBreakers -> "ice_breakers"
        QuestionCategory.Memories -> "memories"
        QuestionCategory.Values -> "values"
        QuestionCategory.FutureDreams -> "future_dreams"
        QuestionCategory.DailyLife -> "daily_life"
        QuestionCategory.Intimacy -> "intimacy"
    }

private fun categoryFromStorageKey(key: String): QuestionCategory =
    when (key) {
        "ice_breakers" -> QuestionCategory.IceBreakers
        "memories" -> QuestionCategory.Memories
        "values" -> QuestionCategory.Values
        "future_dreams" -> QuestionCategory.FutureDreams
        "daily_life" -> QuestionCategory.DailyLife
        "intimacy" -> QuestionCategory.Intimacy
        else -> QuestionCategory.IceBreakers
    }

fun CardEntity.toDomain(): Question =
    Question(
        id = id,
        text = text,
        languageCode = languageCode,
        category = categoryFromStorageKey(category),
    )

fun Question.toEntity(): CardEntity =
    CardEntity(
        id = id,
        text = text,
        languageCode = languageCode,
        category = category.toStorageKey(),
    )
