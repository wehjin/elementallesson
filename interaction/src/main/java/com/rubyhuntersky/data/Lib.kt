package com.rubyhuntersky.data

data class QuizQuestion(
    val prompt: String,
    val target: String
)

data class Quiz(
    val questions: List<QuizQuestion>
)

