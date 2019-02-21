package com.rubyhuntersky.quizmaker

import com.rubyhuntersky.data.Challenge
import com.rubyhuntersky.data.NamedQuiz
import com.rubyhuntersky.data.QuizGroup

object GanbarooQuizGroup : QuizGroup {
    override val name: String = "Ganbaroo"

    override val quizzes = listOf(
        NamedQuiz(
            "Days of the month", listOf(
                Challenge("1st day of the month", "tsuitachi"),
                Challenge("2nd day of the month", "futsuka"),
                Challenge("3rd day of the month", "mikka"),
                Challenge("4th day of the month", "yokka"),
                Challenge("5th day of the month", "itsuka"),
                Challenge("6th day of the month", "muika"),
                Challenge("7th day of the month", "nanoka"),
                Challenge("8th day of the month", "youka"),
                Challenge("9th day of the month", "kokonoka"),
                Challenge("10th day of the month", "tooka"),
                Challenge("11-13, 15, 16, 18, 21-23, 25, 26, 28, 30, 31th day of the month", "# -nichi"),
                Challenge("14th day of the month", "juuyokka"),
                Challenge("17th day of the month", "juushichinichi"),
                Challenge("19th day of the month", "juukunichi"),
                Challenge("20th day of the month", "hatsuka"),
                Challenge("24th day of the month", "nijuuyokka"),
                Challenge("27th day of the month", "nijuushichinichi"),
                Challenge("29th day of the month", "nijuukunichi")
            )
        ),
        NamedQuiz(
            "Frequencies", listOf(
                Challenge("always", "itsumo"),
                Challenge("often", "yoku"),
                Challenge("sometimes", "tokidoki"),
                Challenge("rarely", "amari"),
                Challenge("never", "zenzen")
            )
        ),
        NamedQuiz(
            "Other", listOf(
                Challenge("holiday", "yasumi")
            )
        )
    )
}