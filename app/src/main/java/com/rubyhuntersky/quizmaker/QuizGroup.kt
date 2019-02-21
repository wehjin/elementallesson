package com.rubyhuntersky.quizmaker

import com.rubyhuntersky.data.Challenge
import com.rubyhuntersky.data.NamedQuiz
import com.rubyhuntersky.data.QuizGroup

object GanbarooQuizGroup : QuizGroup {
    override val name: String = "Ganbaroo"

    override val quizzes = listOf(
        NamedQuiz("Chapter 2 Vocabulary", listOf(
            "animation".."anime"
            , "breakfast".."asagohan"
            , "the day after tomorrow".."asatte"
            , "tomorrow".."ashita"
            , "banana".."banana"
            , "dinner".."bangohan"
            , "study".."benkyoo"
            , "beer".."biiru"
            , "department store".."depaato"
            , "DVD".."diibuidii"
            , "movie".."eiga"
            , "football".."futtobooru"
            , "Ginza district".."Ginza"
            , "rice; meal".."gohan"
            , "about (4 o'clock)".."(yoji) -goro"
            , "half".."han"
            , "hamburger".."hanbaagaa"
            , "lunch".."hirugohan"
            , "now".."ima"
            , "(3) o'clock".."(san)-ji"
            , "cafe".."kafe"
            , "karaoke".."karaoke"
            , "coffee".."koohii"
            , "today".."kyoo"
            , "every morning".."maiasa"
            , "every night".."maiban"
            , "every day".."mainichi"
            , "water".."mizu"
            , "what (are you eating?)".."nani (o tabemasu ka?)"
            , "what time".."nanji"
            , "Japantown".."Nihonmachi"
            , "tea; green tea".."ocha"
            , "music".."ongaku"
            , "orange juice".."orenjijuusu"
            , "pasta".."pasuta"
            , "pizza".."piza"
            , "ramen".."raamen"
            , "radio".."rajio"
            , "language center".."rangeejisentaa"
            , "restaurant".."resutoran"
            , "sake; rice wine; alcohol".."(o)sake"
            , "CD".."shiidii"
            , "dining room; cafeteria; restaurant".."shokudoo"
            , "homework".."shukudai"
            , "ski".."sukii"
            , "supermarket".."suupaa"
            , "sushi".."sushi"
            , "sushi restaurant".."sushiya"
            , "tape".."teepu"
            , "tennis".."tenisu"
            , "tempura".."tenpura"
            , "TV".."terebi"
            , "test".."tesuto"
            , "library".."toshokan"
            , "Tsukiji district".."Tsukiji"
            , "wine".."wain"
            , "do study".."benkyoo (o) shimasu"
            , "go".."ikimasu"
            , "return; go back (home)".."kaerimasu"
            , "listen".."kikimasu"
            , "come (to current location, my home)".."kimasu"
            , "see; look; watch".."mimasu"
            , "go to bed; sleep".."nemasu"
            , "drink".."nomimasu"
            , "get up; wake up".."okimasu"
            , "do".."shimasu"
            , "eat".."tabemasu"
            , "(I read) at; in; on (home)".."(uchi) de (yomimasu)"
            , "(it's good) right?".."(ii desu) ne"
            , "(I go) to (the library)".."(toshokan) ni (ikimasu)"
            , "(I drink) <object-tag> (wine)".."(wain) o (nomimasu)"
            , "(I eat sushi) !".."(sushi o tabemasu) yo"
            , "together".."isshoni"
            , "right away".."sugu"
            , "often".."yoku"
            , "a little; while".."chotto..."
            , "It sounds good!".."Ii desu ne"
            , "See you the day after tomorrow".."Jaa mata asatte"
            , "after now".."korekara"
            , " I got hungry".."Onaka ga sukimashita"
            , "then; so".."soreja"
            , "I agree".."Sore ga ii desu ne"
            , "after that; and then".."sorekara"
            , "(I read) and (I write)".."(yomimasu) soshite (kakimasu)"
            , "Um, karaoke is it? (That's difficult)".."Uun, karaoke desu ka? (Chotto..)"
            , "I got thirsty".."Nodo ga kawakimashita"
        ).map { Challenge(it.start, it.endInclusive) }),
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