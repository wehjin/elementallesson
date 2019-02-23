package com.rubyhuntersky.data

data class Challenge(
    val question: String,
    val answer: String
)

data class Quiz(
    val challenges: List<Challenge>
)

data class NamedQuiz(
    val name: String,
    val challenges: List<Challenge>
) {
    fun toQuiz(): Quiz = Quiz(challenges)
}

interface QuizGroup {
    val name: String
    val quizzes: List<NamedQuiz>
}