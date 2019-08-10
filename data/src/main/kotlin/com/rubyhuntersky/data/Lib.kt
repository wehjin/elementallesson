package com.rubyhuntersky.data

data class Course(
    val name: String,
    val lessons: Set<Lesson>
)

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