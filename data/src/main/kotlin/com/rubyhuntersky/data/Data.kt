package com.rubyhuntersky.data

import java.util.*
import kotlin.random.Random

data class Learner(
    private val quizMap: Map<String, Quiz> = emptyMap(),
    private val performedOnMap: Map<String, Date?> = emptyMap()
) {
    val quizzes get() = quizMap.values.toSet()

    val performances: Set<Performance>
        get() = performedOnMap
            .map {
                val quizId = it.key
                val performedOn = it.value
                val quizName = quizMap.getValue(quizId).name
                Performance(performedOn, quizId, quizName)
            }.toSet()

    fun addQuiz(name: String, challenges: List<Challenge>, optionalId: String? = null): Learner {
        val id = optionalId ?: Random.nextLong().toString()
        return Learner(
            quizMap.toMutableMap().apply {
                this[id] = Quiz(id, name, challenges)
            },
            performedOnMap.toMutableMap().apply {
                this[id] = null
            }
        )
    }

    fun addPerformance(quizId: String, performedOn: Date): Learner =
        Learner(
            quizMap,
            performedOnMap.toMutableMap().apply { put(quizId, performedOn) }
        )
}

data class Performance(
    val performedOn: Date?,
    val quizId: String,
    val quizName: String
)

data class Quiz(
    val id: String,
    val name: String,
    val challenges: List<Challenge>
)

data class NamedQuiz(
    val name: String,
    val challenges: List<Challenge>
)

data class ChallengeList(
    val challenges: List<Challenge>
)

data class Challenge(
    val question: String,
    val answer: String
)

interface Publisher {
    val name: String
    val quizzes: List<NamedQuiz>
}

fun NamedQuiz.toQuiz(): ChallengeList = ChallengeList(challenges)
