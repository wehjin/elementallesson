package com.rubyhuntersky.data

import java.util.*

object GanbarooPublisher : Publisher {
    override val name: String = "Ganbaroo"

    override val quizzes = listOf(
        PublishedQuiz("Chapter 3 Vocabulary",
            Calendar.getInstance().apply { set(2019, 1, 23) }.time,
            listOf(
                "barbecue".."baabekyuu"
                , "basketball".."basukettobooru"
                , "bed".."beddo"
                , "rice bowl; teacup".."chawan"
                , "Germany".."Doitsu"
                , "futon".."futon"
                , "foreign country".."gaikoku"
                , "game".."geemu"
                , "language suffix".."-go"
                , "what language".."nanigo"
                , "Harajuku district".."Harajuku"
                , "chopsticks".."hashi"
                , "book".."hon"
                , "bookstore".."hoya"
                , "England".."Igirisu"
                , "jazz".."jazu"
                , "shopping".."kaimono"
                , "this morning".."kesa"
                , "yesterday".."kinoo"
                , "tonight".."konban"
                , "cola".."koora"
                , "email".."meeru"
                , "the day before yesterday".."ototoi"
                , "Playstation".."pureisuteeshon"
                , "salad".."salada"
                , "sandwich".."sandoitchi"
                , "Shibuya district".."Shibuya"
                , "newspaper".."shinbun"
                , "meal".."shokuji"
                , "letter".."tegami"
                , "magazine".."zasshi"
                , "go out".."dekakemasu"
                , "speak; talk".."hanashimasu"
                , "buy".."kaimasu"
                , "do shopping".."kaimono (o) shimasu"
                , "write".."kakimasu"
                , "have a meal".."shokuji (o) shimasu"
                , "read".."yomimasu"
                , "good at; skillful; proficient".."joozu"
                , "a little".."chotto"
                , "definitely".."zehi"
                , "alternate object marker".."ga"
                , "also; too; (not) either".."mo"
                , "and for nouns".."to"
                , "oh?/oh!".."e"
                , "but".."demo"
                , "Is it all right with you?".."Ii n desu ka?"
                , "I appreciate it; I appreciate your offer; Please do it for me".."Onegaishimasu"
                , "not yet".."Mada desu"
                , "Speaking of which".."Soosoo"
                , "Wow!".."uun"
            ).map { Challenge(it.start, it.endInclusive) }),
        PublishedQuiz("Chapter 2 Vocabulary",
            Calendar.getInstance().apply { set(2019, 1, 22) }.time,
            listOf(
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
        PublishedQuiz(
            "Days of the month",
            Calendar.getInstance().apply { set(2019, 1, 20) }.time,
            listOf(
                "1st day of the month".."tsuitachi"
                , "2nd day of the month".."futsuka"
                , "3rd day of the month".."mikka"
                , "4th day of the month".."yokka"
                , "5th day of the month".."itsuka"
                , "6th day of the month".."muika"
                , "7th day of the month".."nanoka"
                , "8th day of the month".."youka"
                , "9th day of the month".."kokonoka"
                , "10th day of the month".."tooka"
                , "11-13, 15,..6, 18, 21-23, 25, 26, 28, 30, 31th day of the month".."# -nichi"
                , "14th day of the month".."juuyokka"
                , "17th day of the month".."juushichinichi"
                , "19th day of the month".."juukunichi"
                , "20th day of the month".."hatsuka"
                , "24th day of the month".."nijuuyokka"
                , "27th day of the month".."nijuushichinichi"
                , "29th day of the month".."nijuukunichi"
            ).map { Challenge(it.start, it.endInclusive) }),
        PublishedQuiz(
            "Frequencies",
            Calendar.getInstance().apply { set(2019, 1, 19) }.time,
            listOf(
                "always".."itsumo"
                , "often".."yoku"
                , "sometimes".."tokidoki"
                , "rarely".."amari"
                , "never".."zenzen"
            ).map { Challenge(it.start, it.endInclusive) }
        ),
        PublishedQuiz(
            "Other",
            Calendar.getInstance().apply { set(2019, 1, 18) }.time,
            listOf(
                "holiday".."yasumi"
            ).map { Challenge(it.start, it.endInclusive) }
        )
    )
}