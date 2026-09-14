package org.neteinstein.couples.feature.game

/**
 * Static "Do we know each other well?" card content for the Game tab - unlike
 * [org.neteinstein.couples.feature.home.HomeScreen]'s questions, this set is fixed, unfiltered,
 * and not persisted/tracked as used, so it lives here as plain Kotlin rather than seeded into the
 * Room database.
 */
object GameQuestions {
    val all =
        listOf(
            GameQuestion(id = 1, text = "What's my favorite dish?"),
            GameQuestion(id = 2, text = "What's my favorite movie?"),
            GameQuestion(id = 3, text = "What's my favorite music?"),
            GameQuestion(id = 4, text = "What's my favorite flower?"),
            GameQuestion(id = 5, text = "What's my favorite book?"),
            GameQuestion(id = 6, text = "What's my full name?"),
            GameQuestion(id = 7, text = "What's my birthday?"),
            GameQuestion(id = 8, text = "What date did we meet?"),
            GameQuestion(id = 9, text = "What's my favorite color?"),
            GameQuestion(id = 10, text = "What was the first meal we ate together? And what did we drink?"),
            GameQuestion(id = 11, text = "What's the app I spend the most time on, on my phone?"),
            GameQuestion(id = 12, text = "When and where did we first kiss?"),
            GameQuestion(id = 13, text = "What do I love about you?"),
            GameQuestion(id = 14, text = "What would I like you to have less of?"),
            GameQuestion(id = 15, text = "What do I see you doing in 20 years?"),
            GameQuestion(id = 16, text = "How can you make me happy?"),
            GameQuestion(id = 17, text = "Feet or hands?"),
            GameQuestion(id = 18, text = "What's my favorite guilty pleasure?"),
            GameQuestion(id = 19, text = "What's the sentence I say to you the most?"),
            GameQuestion(id = 20, text = "Who's my celebrity crush?"),
            GameQuestion(id = 21, text = "What's the color of my underwear right now?"),
            GameQuestion(id = 22, text = "Love or passion?"),
            GameQuestion(id = 23, text = "What part of my body do I love for you to touch? And which part do I not really like?"),
        )
}
