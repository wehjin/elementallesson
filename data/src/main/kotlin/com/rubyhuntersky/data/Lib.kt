package com.rubyhuntersky.data

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val chapter10CourseMaterial = CourseMaterial(
    title = "Chapter 10",
    subtitle = "Ganbaroo 1",
    lessons = listOf(
        LessonMaterial(
            prompt = "someone else's house",
            response = "おたく",
            promptColor = "> May I escort you home?",
            responseColor = "May I escort you home?\nおたくまでおくりましょうか。"
        ),
        LessonMaterial(prompt = "sound", response = "おと"),
        LessonMaterial(prompt = "adult", response = "大人", responseColor = "（おとな）"),
        LessonMaterial(prompt = "online", response = "オンライン"),
        LessonMaterial(prompt = "semester", response = "学き", responseColor = "（がっき）"),
        LessonMaterial(prompt = "chewing gum", response = "ガム"),
        LessonMaterial(prompt = "cell phone", response = "けいたい"),
        LessonMaterial(prompt = "textbook", response = "きょうかしょ"),
        LessonMaterial(prompt = "classroom", response = "きょうしつ"),
        LessonMaterial(prompt = "blackboard", response = "こくばん"),
        LessonMaterial(prompt = "syllabus", response = "シラバス"),
        LessonMaterial(prompt = "sports car", response = "スポーツカー"),
        LessonMaterial(prompt = "slippers", response = "スリッパ"),
        LessonMaterial(prompt = "last semester", response = "先学き", responseColor = "（せんがっき）"),
        LessonMaterial(prompt = "hand; arm", response = "て"),
        LessonMaterial(prompt = "something", response = "何か", responseColor = "（なにか）"),
        LessonMaterial(prompt = "number", response = "ばんごう"),
        LessonMaterial(prompt = "secret", response = "ひみつ"),
        LessonMaterial(prompt = "present; gift", response = "プレゼント"),
        LessonMaterial(prompt = "bath", response = "（お）ふろ"),
        LessonMaterial(prompt = "boss", response = "ボス"),
        LessonMaterial(prompt = "window", response = "まど"),
        LessonMaterial(prompt = "open", response = "開ける", responseColor = "ru verb"),
        LessonMaterial(prompt = "be late for; be delayed", response = "おくれる", responseColor = "ru verb"),
        LessonMaterial(prompt = "tell", response = "おしえる", responseColor = "ru verb"),
        LessonMaterial(prompt = "close; shut", response = "しめる", responseColor = "ru verb"),
        LessonMaterial(prompt = "inform", response = "知らせる", responseColor = "ru verb\n（しらせる）"),
        LessonMaterial(prompt = "turn on", response = "わける", responseColor = "ru verb"),
        LessonMaterial(prompt = "forget", response = "わすれる", responseColor = "ru verb"),
        LessonMaterial(prompt = "say", response = "言う", responseColor = "u verb\n（いう)"),
        LessonMaterial(prompt = "finish; end", response = "おわる", responseColor = "u verb"),
        LessonMaterial(prompt = "chew", response = "かむ", responseColor = "u verb"),
        LessonMaterial(prompt = "do one's best", response = "がんばる", responseColor = "u verb"),
        LessonMaterial(prompt = "turn off; erase", response = "けす", responseColor = "u verb"),
        LessonMaterial(prompt = "smoke; inhale", response = "すう", responseColor = "u verb"),
        LessonMaterial(prompt = "use", response = "つかう", responseColor = "u verb"),
        LessonMaterial(prompt = "take", response = "とる", responseColor = "u verb"),
        LessonMaterial(prompt = "begin", response = "はじまる", responseColor = "u verb"),
        LessonMaterial(prompt = "be absent from", response = "休む", responseColor = "u verb\n（やすむ）"),
        LessonMaterial(prompt = "explain", response = "せつめい（を）する", responseColor = "irregular verb"),
        LessonMaterial(prompt = "consult", response = "そうだん（を）する", responseColor = "irregular verb"),
        LessonMaterial(prompt = "practice", response = "れんしゅう（を）する", responseColor = "irregular verb"),
        LessonMaterial(prompt = "contact", response = "れんらく（を）する", responseColor = "irregular verb"),
        LessonMaterial(prompt = "important", response = "大せつ", responseColor = "na adjective\n（たいせつ）"),
        LessonMaterial(prompt = "hard", response = "大へん", responseColor = "na adjective\n（たいへん）"),
        LessonMaterial(prompt = "no good; bad", response = "だめ", responseColor = "na adjective"),
        LessonMaterial(prompt = "always", response = "いつも"),
        LessonMaterial(prompt = "without fail; surely", response = "かならず"),
        LessonMaterial(prompt = "properly", response = "ちゃんと"),
        LessonMaterial(prompt = "yet", response = "まだ"),
        LessonMaterial(prompt = "already", response = "もう"),
        LessonMaterial(prompt = "okay; OK; alright", response = "オーケー"),
        LessonMaterial(prompt = "Let's study hard.", response = "がんばってべんきょうしましょう。"),
        LessonMaterial(prompt = "under any circumstance; definitely", response = "ぜったいに"),
        LessonMaterial(prompt = "but", response = "だけど"),
        LessonMaterial(prompt = "next; next time", response = "つぎに"),
        LessonMaterial(prompt = "all together", response = "みんなで"),
        LessonMaterial(prompt = "of course", response = "もしろん")
    )
)

@Serializer(forClass = LocalDateTime::class)
object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), DateTimeFormatter.ISO_DATE_TIME)
    }

    override fun serialize(encoder: Encoder, obj: LocalDateTime) {
        encoder.encodeString(obj.format(DateTimeFormatter.ISO_DATE_TIME))
    }
}
