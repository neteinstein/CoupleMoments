package org.neteinstein.couples.domain.model

sealed class QuestionCategory(
    val emoji: String,
) {
    data object IceBreakers : QuestionCategory("🎉")

    data object Memories : QuestionCategory("📸")

    data object Values : QuestionCategory("❤️")

    data object FutureDreams : QuestionCategory("🔮")

    data object DailyLife : QuestionCategory("🌻")

    data object Intimacy : QuestionCategory("💕")

    companion object {
        val all: List<QuestionCategory> = listOf(IceBreakers, Memories, Values, FutureDreams, DailyLife, Intimacy)
    }
}
