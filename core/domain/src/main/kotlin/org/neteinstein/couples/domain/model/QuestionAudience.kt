package org.neteinstein.couples.domain.model

/** Which couples a [Question] is written for, based on whether they have kids. */
enum class QuestionAudience {
    WithoutKids,
    WithKids,
    Both,
}
