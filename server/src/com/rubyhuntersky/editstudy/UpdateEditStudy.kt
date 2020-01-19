package com.rubyhuntersky.editstudy

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
    val studyNumber = call.parameters["study"]?.toLongOrNull()
    return studyNumber?.let {
        if (call.request.isMultipart()) {
            call.receiveMultipart().forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> TODO()
                    is PartData.FileItem -> {
                        val originalName = part.originalFileName ?: error("No original file name: $part")
                        println("ORIGINAL NAME: $originalName")
                        part.streamProvider().use { inputStream ->
                            val element = JsonParser().parse(inputStream.bufferedReader()).asJsonObject
                            println("ELEMENT: $element")
                            val level = element["level"].asLong
                            println("LEVEL: $level")
                        }
                    }
                    is PartData.BinaryItem -> TODO()
                }
                part.dispose()
            }
            null
        } else {
            editStudyAction(call.receive(), it, learnerNumber)
        }
    }
}
