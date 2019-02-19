package com.rubyhuntersky.data

data class Challenge(
    val question: String,
    val answer: String
)

data class Quiz(
    val challenges: List<Challenge>
)

