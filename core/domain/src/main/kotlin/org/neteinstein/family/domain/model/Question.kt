package org.neteinstein.family.domain.model

data class Question(val id: Int, val text: String, val languageCode: String, val category: QuestionCategory = QuestionCategory.IceBreakers)
