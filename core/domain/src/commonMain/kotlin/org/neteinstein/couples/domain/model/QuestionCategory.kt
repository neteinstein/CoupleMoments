package org.neteinstein.couples.domain.model

/**
 * The icon shown for each category is chosen in the UI layer (`feature:home`'s `HomeScreen.kt`) -
 * `core:domain` has no Compose dependency, so it can't hold an `ImageVector` here. This used to
 * carry a literal emoji glyph instead, but Compose Multiplatform's Wasm/Skia text renderer has no
 * emoji-font fallback (unlike Android/iOS, which both resolve emoji through the OS's own font
 * fallback), so those glyphs rendered as "tofu" boxes on the web build - vector icons render
 * identically on every target.
 */
sealed class QuestionCategory {
    data object IceBreakers : QuestionCategory()

    data object Memories : QuestionCategory()

    data object Values : QuestionCategory()

    data object FutureDreams : QuestionCategory()

    data object DailyLife : QuestionCategory()

    data object Intimacy : QuestionCategory()

    companion object {
        val all: List<QuestionCategory> = listOf(IceBreakers, Memories, Values, FutureDreams, DailyLife, Intimacy)
    }
}
