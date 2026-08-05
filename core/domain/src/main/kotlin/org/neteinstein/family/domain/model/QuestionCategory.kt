package org.neteinstein.family.domain.model

sealed class QuestionCategory(val label: String) {
    data object IceBreakers : QuestionCategory("🎉 Ice Breakers")
    data object Memories : QuestionCategory("📸 Memories")
    data object Values : QuestionCategory("❤️ Values")
    data object FutureDreams : QuestionCategory("🔮 Future Dreams")
    data object DailyLife : QuestionCategory("🌻 Daily Life")
}
