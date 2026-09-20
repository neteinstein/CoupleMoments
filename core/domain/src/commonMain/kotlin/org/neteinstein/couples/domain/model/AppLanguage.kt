package org.neteinstein.couples.domain.model

/**
 * Languages [org.neteinstein.couples.domain.repository.QuestionRepository] has question content
 * for (see `core:data`'s `QuestionSeedData`) and the UI has translations for. [code] is the bare
 * ISO 639-1 language code `core:data`'s `LocaleProvider`/`QuestionRepository` already key on - no
 * region subtag. [nativeName] is each language's own name for itself (not translated), the
 * conventional way to label a language picker so every entry is legible regardless of which
 * language is currently active.
 */
enum class AppLanguage(
    val code: String,
    val nativeName: String,
) {
    ENGLISH("en", "English"),
    PORTUGUESE("pt", "Português"),
    SPANISH("es", "Español"),
    FRENCH("fr", "Français"),
    GERMAN("de", "Deutsch"),
    ;

    companion object {
        val Default = ENGLISH

        fun fromCode(code: String): AppLanguage? = entries.firstOrNull { it.code == code }
    }
}
