package com.rubyhuntersky.dostudy

import com.rubyhuntersky.data.v2.*
import com.rubyhuntersky.fillQuota
import com.rubyhuntersky.tomedb.Peer
import com.rubyhuntersky.tomedb.get
import com.rubyhuntersky.tomedb.minion.Leader
import com.rubyhuntersky.tomedb.minion.Minion
import com.rubyhuntersky.tomedb.minion.minionOrNull
import com.rubyhuntersky.tomedb.minion.reformMinion
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.pow
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlin.time.days

data class ActionRender(
    val action: DoStudyAction?,
    val render: Channel<DoStudyVision?>?
)

data class DoStudyStory(
    val number: Int,
    val messages: Channel<ActionRender>
)

@ExperimentalTime
fun doStudy(
    studyNumber: Long,
    learner: Peer<Learner.Name, String>,
    firstRender: Channel<DoStudyVision?>
): DoStudyStory {
    val messages = Channel<ActionRender>(10)
    suspend fun receiveActionRender(oldVision: DoStudyVision?): Pair<DoStudyAction, Channel<DoStudyVision?>?> {
        while (true) {
            val (action, render) = messages.receive()
            if (action == null) {
                render?.send(oldVision)
            } else {
                return Pair(action, render)
            }
        }
    }
    GlobalScope.launch {
        var vision: DoStudyVision? = learnerScope(learner) {
            initDoStudy(studyNumber, Date())
        }.also { firstRender.send(it) }
        while (vision != null) {
            val oldVision = vision
            val (action, render) = receiveActionRender(oldVision)
            vision = learnerScope(learner) {
                updateDoStudy(oldVision, action)
            }.also { render?.send(it) }
        }
    }
    return DoStudyStory(Random.nextInt(), messages)
}

@ExperimentalTime
fun LearnerScope.initDoStudy(
    studyNumber: Long,
    now: Date,
    completed: List<Long>? = null
): ReadyToStudy {
    val db = tomic.latest
    val study = db.readStudy(studyNumber, learner.ent) ?: error("Study not found: $studyNumber")
    val assessments = db.readAssessments(study)
    val (new, awake, resting) = assessments.newAwakeResting(now)
    return ReadyToStudy(
        learnerNumber = learner.ent,
        learnerName = learner[Learner.Name],
        studyNumber = studyNumber,
        studyName = study[Study.Name],
        newCount = new.size,
        awakeCount = awake.size,
        restCount = resting.size,
        completed = completed ?: emptyList()
    )
}

@ExperimentalTime
fun LearnerScope.updateDoStudy(vision: DoStudyVision, action: DoStudyAction): DoStudyVision {
    return when {
        vision is ReadyToStudy && action is StartStudy -> startStudy(vision, action)
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
                initDoStudy(vision.studyNumber, Date(), done)
            } else {
                Studying(
                    learnerNumber = vision.learnerNumber,
                    learnerName = vision.learnerName,
                    studyNumber = vision.studyNumber,
                    studyName = vision.studyName,
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
private fun LearnerScope.startStudy(vision: DoStudyVision, action: StartStudy): DoStudyVision {
    val db = tomic.latest
    val now = Date()
    val study = db.readStudy(vision.studyNumber, vision.learnerNumber)
        ?: error("Study not found: ${vision.studyNumber}")
    val assessments = db.readAssessments(study)
    val (new, awake) = assessments.newAwakeResting(now)
    val (newQuota, awakeQuota) = fillQuota(action.count, action.split, shuffleLevels(new), awake)
    val combined = (newQuota + awakeQuota).shuffled()
    val first = combined.firstOrNull()
    return if (first == null) vision
    else Studying(
        learnerNumber = vision.learnerNumber,
        learnerName = vision.learnerName,
        studyNumber = vision.studyNumber,
        studyName = vision.studyName,
        assessmentVision = vision(first),
        todo = combined.mapNotNull { if (it.ent == first.ent) null else it.ent },
        done = emptyList()
    )
}


@ExperimentalTime
private fun Set<Minion<Assessment.Study>>.newAwakeResting(now: Date): Triple<List<Minion<Assessment.Study>>, List<Minion<Assessment.Study>>, List<Minion<Assessment.Study>>> {
    val (new, passed) = this.partition { passCount(it) == 0L }
    val (awake, resting) = passed.partition { wakeTime(it).before(now) }
    return Triple(new, awake, resting)
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
