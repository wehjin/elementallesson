package com.rubyhuntersky.editstudy

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.rubyhuntersky.data.v2.createClozeAssessment
import com.rubyhuntersky.data.v2.createListenAssessment
import com.rubyhuntersky.data.v2.createProduceAssessment
import com.rubyhuntersky.data.v2.readStudy
import com.rubyhuntersky.tomedb.Tomic
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.request.isMultipart
import io.ktor.request.receive
import io.ktor.request.receiveMultipart

sealed class EditStudyAction {
    abstract val studyNumber: Long
    abstract val learnerNumber: Long
}

data class CreateProduceAssessment(
    override val studyNumber: Long,
    override val learnerNumber: Long,
    val prompt: String,
    val response: String,
    val level: Long
) : EditStudyAction()

data class CreateListenAssessment(
    override val studyNumber: Long,
    override val learnerNumber: Long,
    val search: String,
    val response: String,
    val level: Long
) : EditStudyAction()

data class CreateClozeAssessment(
    override val studyNumber: Long,
    override val learnerNumber: Long,
    val template: String,
    val fill: String,
    val level: Long
) : EditStudyAction()

data class ImportJsonAssessments(
    override val studyNumber: Long,
    override val learnerNumber: Long,
    val jsonObject: JsonObject
) : EditStudyAction()

fun updateEditStudyModel(tomic: Tomic, action: EditStudyAction) {
    val study = tomic.latest.readStudy(action.studyNumber, action.learnerNumber)
    study?.let {
        when (action) {
            is CreateProduceAssessment -> {
                tomic.createProduceAssessment(it, action.response, action.prompt, action.level)
            }
            is CreateListenAssessment -> {
                tomic.createListenAssessment(it, action.response, action.search, action.level)
            }
            is CreateClozeAssessment -> {
                tomic.createClozeAssessment(it, action.fill, action.template, action.level)
            }
            is ImportJsonAssessments -> {
                println("JSON-ASSESSMENTS: ${action.jsonObject}")
            }
        }
    }
}

fun editStudyAction(params: Parameters, studyNumber: Long, learnerNumber: Long): EditStudyAction? {
    val productionResponse = params["production_response"]
    val listenResponse = params["listen_response"]
    val clozeResponse = params["cloze_fill"]
    return when {
        productionResponse != null -> {
            val prompt = params["prompt"] ?: "UNKNOWN"
            val level = params["level"]?.toLongOrNull() ?: 0
            CreateProduceAssessment(studyNumber, learnerNumber, prompt, productionResponse, level)
        }
        listenResponse != null -> {
            val prompt = params["listen_prompt"] ?: "うわー"
            val level = params["listen_level"]?.toLongOrNull() ?: 0
            CreateListenAssessment(studyNumber, learnerNumber, listenResponse, prompt, level)
        }
        clozeResponse != null -> {
            val prompt = params["cloze_template"] ?: "{..}"
            val level = params["cloze_level"]?.toLongOrNull() ?: 0
            CreateClozeAssessment(studyNumber, learnerNumber, clozeResponse, prompt, level)
        }
        else -> null
    }
}

suspend fun editStudyAction(call: ApplicationCall, learnerNumber: Long): EditStudyAction? {
    return call.parameters["study"]?.toLongOrNull()?.let { studyNumber ->
        if (call.request.isMultipart()) {
            var element: JsonObject? = null
            call.receiveMultipart().forEachPart { part ->
                if (element == null) {
                    when (part) {
                        is PartData.FileItem -> part.streamProvider().use {
                            element = JsonParser().parse(it.bufferedReader()).asJsonObject
                        }
                        is PartData.FormItem -> error("Multipart form items are not supported")
                        is PartData.BinaryItem -> error("Multipart binary items are not supported")
                    }
                }
                part.dispose()
            }
            element?.let { ImportJsonAssessments(studyNumber, learnerNumber, it) }
        } else {
            editStudyAction(call.receive(), studyNumber, learnerNumber)
        }
    }
}
