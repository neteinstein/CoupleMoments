package org.neteinstein.couples.data.local

import org.junit.Assert.assertEquals
import org.junit.Test
import org.neteinstein.couples.domain.model.Question
import org.neteinstein.couples.domain.model.QuestionAudience
import org.neteinstein.couples.domain.model.QuestionCategory

class CardMapperTest {
    @Test
    fun `toEntity and toDomain round-trip every audience value`() {
        QuestionAudience.entries.forEach { audience ->
            val question =
                Question(
                    id = 1,
                    text = "Question?",
                    languageCode = "en",
                    category = QuestionCategory.Memories,
                    audience = audience,
                )

            assertEquals(question, question.toEntity().toDomain())
        }
    }

    @Test
    fun `toDomain defaults to Both for an unrecognized stored audience key`() {
        val entity = CardEntity(id = 1, text = "Question?", languageCode = "en", category = "memories", audience = "unknown")

        assertEquals(QuestionAudience.Both, entity.toDomain().audience)
    }
}
