package com.rubyhuntersky.dostudy

sealed class DoStudyAction
data class StartStudy(val count: Int = 20, val split: Float = 0.5f) : DoStudyAction()
data class RecordAssessment(val passed: Boolean) : DoStudyAction()
