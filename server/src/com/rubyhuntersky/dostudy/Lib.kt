package com.rubyhuntersky.dostudy

import com.rubyhuntersky.data.v2.Assessment
import com.rubyhuntersky.data.v2.readAssessments
import com.rubyhuntersky.data.v2.readStudy
import com.rubyhuntersky.fillQuota
import com.rubyhuntersky.tomedb.database.Database
import com.rubyhuntersky.tomedb.get
import com.rubyhuntersky.tomedb.minion.Leader
import com.rubyhuntersky.tomedb.minion.Minion
import com.rubyhuntersky.tomedb.minion.minionOrNull
import com.rubyhuntersky.tomedb.minion.reformMinion
import com.rubyhuntersky.tomic
import java.util.*
import kotlin.math.pow
import kotlin.time.ExperimentalTime
import kotlin.time.days

sealed class DoStudyVision {
    abstract val learnerNumber: Long
    abstract val studyNumber: Long
}

data class ReadyToStudy(
    override val learnerNumber: Long,
    override val studyNumber: Long,
    val reviewCount: Int,
    val newCount: Int,
    val completed: List<Long>
) : DoStudyVision()

data class Studying(
    override val learnerNumber: Long,
    override val studyNumber: Long,
    val assessmentVision: AssessmentVision,
    val todo: List<Long>,
    val done: List<Long>
) : DoStudyVision()

sealed class DoStudyAction
data class StartStudy(val count: Int = 20, val split: Float = 0.5f) : DoStudyAction()
data class RecordAssessment(val passed: Boolean) : DoStudyAction()

@ExperimentalTime
fun updateDoStudy(vision: DoStudyVision, action: DoStudyAction): DoStudyVision {
    val db = tomic.latest
    return when {
        vision is ReadyToStudy && action is StartStudy -> {
            val now = Date()
            val assessments =
                assessments(vision.studyNumber, vision.learnerNumber, db)
            val (new, awake) = assessments.newAndAwake(now)
            val (newQuota, awakeQuota) = fillQuota(
                action.count,
                action.split,
                shuffleLevels(new),
                awake
            )
            val combined = (newQuota + awakeQuota).shuffled()
            val first = combined.firstOrNull()
            if (first == null) vision
            else Studying(
                learnerNumber = vision.learnerNumber,
                studyNumber = vision.studyNumber,
                assessmentVision = vision(first),
                todo = combined.mapNotNull { if (it.ent == first.ent) null else it.ent },
                done = emptyList()
            )
        }
        vision is Studying && action is RecordAssessment -> {
            val previous = vision.assessmentVision
            tomic.reformMinion(Leader(vision.studyNumber, Assessment.Study), previous.assessmentNumber) {
                if (action.passed) {
                    Assessment.PassCount set (previous.passCount + 1)
                    Assessment.PassTime set Date()
                } else {
                    Assessment.PassCount set 0
                }
            }
            val done = vision.done + previous.assessmentNumber
            val next = vision.todo.firstOrNull()
                ?.let { tomic.latest.minionOrNull(Leader(vision.studyNumber, Assessment.Study), it) }
            if (next == null) {
                initDoStudy(
                    vision.studyNumber,
                    vision.learnerNumber,
                    Date(),
                    done
                )
            } else {
                Studying(
                    learnerNumber = vision.learnerNumber,
                    studyNumber = vision.studyNumber,
                    assessmentVision = vision(next),
                    todo = vision.todo.drop(1),
                    done = done
                )
            }
        }
        else -> error("Not implemented: $action $vision")
    }
}


@ExperimentalTime
fun initDoStudy(studyNumber: Long, learnerNumber: Long, now: Date, completed: List<Long>? = null): DoStudyVision {
    val db = tomic.latest
    val assessments = assessments(studyNumber, learnerNumber, db)
    val (new, awake) = assessments.newAndAwake(now)
    return ReadyToStudy(
        learnerNumber,
        studyNumber,
        awake.size,
        new.size,
        completed ?: emptyList()
    )
}

@ExperimentalTime
private fun Set<Minion<Assessment.Study>>.newAndAwake(now: Date): Pair<List<Minion<Assessment.Study>>, List<Minion<Assessment.Study>>> {
    val (new, passed) = this.partition { passCount(it) == 0L }
    val (awake, _) = passed.partition { wakeTime(it).before(now) }
    return Pair(new, awake)
}

@ExperimentalTime
private fun wakeTime(assessment: Minion<Assessment.Study>): Date {
    val passCount = passCount(assessment)
    return if (passCount < 1L) {
        Date(0)
    } else {
        val restDuration = 2f.pow(passCount.toFloat()).toInt().days
        val restStart = assessment[Assessment.PassTime] ?: Date(0)
        Date(restStart.time + restDuration.inMilliseconds.toLong())
    }
}

private fun vision(assessment: Minion<Assessment.Study>): AssessmentVision = when {
    assessment[Assessment.Prompt] != null -> ProduceVision(
        assessmentNumber = assessment.ent,
        passCount = passCount(assessment),
        prompt = assessment[Assessment.Prompt] ?: "no-prompt",
        response = assessment[Assessment.ProductionResponse] ?: "no-response"
    )
    assessment[Assessment.ListenPrompt] != null -> ListenVision(
        assessmentNumber = assessment.ent,
        passCount = passCount(assessment),
        search = assessment[Assessment.ListenPrompt] ?: "no-search",
        response = assessment[Assessment.ListenResponse] ?: "no-response"
    )
    assessment[Assessment.ClozeTemplate] != null -> ClozeVision(
        assessmentNumber = assessment.ent,
        passCount = passCount(assessment),
        template = assessment[Assessment.ClozeTemplate] ?: "no-template",
        fill = assessment[Assessment.ClozeFill] ?: "no-fill"
    )
    else -> error("Invalid Assessment: $assessment")
}

private fun passCount(assessment: Minion<Assessment.Study>): Long = assessment[Assessment.PassCount] ?: 0L

private fun shuffleLevels(assessments: List<Minion<Assessment.Study>>): List<Minion<Assessment.Study>> {
    val byLevel = assessments.groupBy { it[Assessment.Level] ?: 0L }
    return byLevel.keys.sorted().mapNotNull { byLevel[it]?.shuffled() }.flatten()
}

private fun assessments(studyNumber: Long, learnerNumber: Long, db: Database): Set<Minion<Assessment.Study>> {
    val study = db.readStudy(studyNumber, learnerNumber) ?: error("Study not found: $studyNumber")
    return db.readAssessments(study)
}
