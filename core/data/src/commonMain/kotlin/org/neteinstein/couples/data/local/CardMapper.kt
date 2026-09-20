package org.neteinstein.couples.data.local

import org.neteinstein.couples.domain.model.Question
import org.neteinstein.couples.domain.model.QuestionAudience
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

private fun QuestionAudience.toStorageKey(): String =
    when (this) {
        QuestionAudience.WithoutKids -> "without_kids"
        QuestionAudience.WithKids -> "with_kids"
        QuestionAudience.Both -> "both"
    }

private fun audienceFromStorageKey(key: String): QuestionAudience =
    when (key) {
        "without_kids" -> QuestionAudience.WithoutKids
        "with_kids" -> QuestionAudience.WithKids
        "both" -> QuestionAudience.Both
        else -> QuestionAudience.Both
    }

fun CardEntity.toDomain(): Question =
    Question(
        id = id,
        text = text,
        languageCode = languageCode,
        category = categoryFromStorageKey(category),
        audience = audienceFromStorageKey(audience),
    )

fun Question.toEntity(): CardEntity =
    CardEntity(
        id = id,
        text = text,
        languageCode = languageCode,
        category = category.toStorageKey(),
        audience = audience.toStorageKey(),
    )
