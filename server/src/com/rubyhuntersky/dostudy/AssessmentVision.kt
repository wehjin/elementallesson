package com.rubyhuntersky.dostudy

sealed class AssessmentVision {
    abstract val assessmentNumber: Long
    abstract val passCount: Long
}

data class ProduceVision(
    override val assessmentNumber: Long,
    override val passCount: Long,
    val prompt: String,
    val response: String
) : AssessmentVision()

data class ListenVision(
    override val assessmentNumber: Long,
    override val passCount: Long,
    val search: String,
    val response: String
) : AssessmentVision()

data class ClozeVision(
    override val assessmentNumber: Long,
    override val passCount: Long,
    val template: String,
    val fill: String
) : AssessmentVision()
