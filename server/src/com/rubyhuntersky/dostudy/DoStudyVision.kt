package com.rubyhuntersky.dostudy

sealed class DoStudyVision {
    abstract val learnerNumber: Long
    abstract val learnerName: String?
    abstract val studyNumber: Long
    abstract val studyName: String?
}

data class ReadyToStudy(
    override val learnerNumber: Long,
    override val learnerName: String?,
    override val studyNumber: Long,
    override val studyName: String?,
    val newCount: Int,
    val awakeCount: Int,
    val restCount: Int,
    val completed: List<Long>
) : DoStudyVision()

data class Studying(
    override val learnerNumber: Long,
    override val learnerName: String?,
    override val studyNumber: Long,
    override val studyName: String?,
    val assessmentVision: AssessmentVision,
    val todo: List<Long>,
    val done: List<Long>
) : DoStudyVision()