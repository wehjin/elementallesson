package com.rubyhuntersky.data

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val chapter10CourseMaterial = CourseMaterial(
    name = "Ganbaroo1 Chapter 10",
    lessons = listOf(
        LessonMaterial(
            prompt = "someone else's house",
            response = "おたく",
            promptColor = "_ _ _ までおくりましょうか。",
            responseColor = "Let me take you home."
        ),
        LessonMaterial(prompt = "sound", response = "おと"),
        LessonMaterial(prompt = "adult", response = "大人（おとな）"),
        LessonMaterial(prompt = "online", response = "オンライン"),
        LessonMaterial(prompt = "semester", response = "学き（がっき"),
        LessonMaterial(prompt = "chewing gum", response = "ガム"),
        LessonMaterial(prompt = "cell phone", response = "けいたい"),
        LessonMaterial(prompt = "textbook", response = "きょうかしょ"),
        LessonMaterial(prompt = "classroom", response = "きょうしつ"),
        LessonMaterial(prompt = "blackboard", response = "こくばん"),
        LessonMaterial(prompt = "syllabus", response = "シラバス"),
        LessonMaterial(prompt = "sports car", response = "スポーツカー"),
        LessonMaterial(prompt = "slippers", response = "スリッパ"),
        LessonMaterial(prompt = "last semester", response = "先学き（せんがっき）"),
        LessonMaterial(prompt = "hand; arm", response = "て"),
        LessonMaterial(prompt = "something", response = "何か（なにか）"),
        LessonMaterial(prompt = "number", response = "ばんごう"),
        LessonMaterial(prompt = "secret", response = "ひみつ"),
        LessonMaterial(prompt = "present; gift", response = "プレゼント"),
        LessonMaterial(prompt = "bath", response = "（お）ふろ"),
        LessonMaterial(prompt = "boss", response = "ボス"),
        LessonMaterial(prompt = "window", response = "まど")
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
