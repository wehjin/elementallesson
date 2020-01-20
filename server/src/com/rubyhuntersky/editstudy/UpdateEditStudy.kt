package com.rubyhuntersky.editstudy

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.rubyhuntersky.data.v2.*
import com.rubyhuntersky.tomedb.Tomic
import com.rubyhuntersky.tomedb.get
import com.rubyhuntersky.tomedb.minion.Leader
import com.rubyhuntersky.tomedb.minion.Minion
import com.rubyhuntersky.tomedb.minion.reformMinions
import com.rubyhuntersky.tomedb.minion.unform
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.request.isMultipart
import io.ktor.request.receive
import io.ktor.request.receiveMultipart
import java.util.*

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
    if (study != null) {
        when (action) {
            is CreateProduceAssessment -> {
                tomic.createProduceAssessment(study, action.response, action.prompt, action.level)
            }
            is CreateListenAssessment -> {
                tomic.createListenAssessment(study, action.response, action.search, action.level)
            }
            is CreateClozeAssessment -> {
                tomic.createClozeAssessment(study, action.fill, action.template, action.level)
            }
            is ImportJsonAssessments -> tomic.importJsonAssessments(study, action.jsonObject)
        }
    }
}

private fun Tomic.importJsonAssessments(study: Minion<Study.Owner>, jsonObject: JsonObject) {
    val assessments = latest.readAssessments(study)
    val level = jsonObject["level"].asLong
    reformMinions(Leader(study.ent, Assessment.Study)) {
        val produceListen = jsonObject["produce-listen"].asJsonArray
        val listenReforms = Unit.let {
            val existingListen = assessments
                .filter {
                    (it[Assessment.Level] ?: -1L) == level
                            && it[Assessment.Source] == "import"
                            && it[Assessment.ListenPrompt] != null
                }
                .associateBy {
                    val search = it[Assessment.ListenPrompt] ?: "no-search"
                    val response = it[Assessment.ListenResponse] ?: "no-response"
                    ListenKey(search, response, level)
                }
            val byListenKey = produceListen.associateBy {
                ListenKey(listenSearch(it), listenResponse(it), level)
            }
            val addsAndUpdates = byListenKey.entries.map { (key, _) ->
                val exists = existingListen[key]
                if (exists == null) {
                    formMinion {
                        Assessment.ListenResponse set key.response
                        Assessment.ListenPrompt set key.search
                        Assessment.Level set level
                        Assessment.Creation set Date()
                        Assessment.Source set "import"
                    }
                } else {
                    reformMinion(exists.ent) {
                        if (minion[Assessment.ListenResponse] != key.response) Assessment.ProductionResponse set key.response
                        if (minion[Assessment.ListenPrompt] != key.search) Assessment.Prompt set key.search
                    }
                }
            }.flatten()
            val removes = existingListen.filter { (key, _) -> !byListenKey.keys.contains(key) }
                .map { (_, minion) -> unform(minion) }
                .flatten()
            addsAndUpdates + removes
        }
        val produceReforms = Unit.let {
            val existingProduce = assessments
                .filter {
                    (it[Assessment.Level] ?: -1L) == level
                            && it[Assessment.Source] == "import"
                            && it[Assessment.Prompt] != null
                }
                .associateBy {
                    val prompt = it[Assessment.Prompt] ?: "Untitled"
                    val response = it[Assessment.ProductionResponse] ?: "Unanswered"
                    ProduceKey(prompt, response, level)
                }
            val byProduceKey = produceListen.associateBy { ProduceKey(producePrompt(it), produceResponse(it), level) }
            val addsAndUpdates = byProduceKey.entries.map { (key, _) ->
                val exists = existingProduce[key]
                if (exists == null) {
                    formMinion {
                        Assessment.ProductionResponse set key.response
                        Assessment.Prompt set key.prompt
                        Assessment.Level set level
                        Assessment.Creation set Date()
                        Assessment.Source set "import"
                    }
                } else {
                    reformMinion(exists.ent) {
                        if (minion[Assessment.ProductionResponse] != key.response) Assessment.ProductionResponse set key.response
                        if (minion[Assessment.ProductionResponse] != key.prompt) Assessment.Prompt set key.prompt
                    }
                }
            }.flatten()
            val removes = existingProduce.filter { (key, _) -> !byProduceKey.keys.contains(key) }
                .map { (_, minion) -> unform(minion) }
                .flatten()
            addsAndUpdates + removes
        }
        reforms = produceReforms + listenReforms
    }
}

private fun producePrompt(it: JsonElement?): String = (it as JsonObject)["from"].asString
private fun produceResponse(it: JsonElement?): String = (it as JsonObject)["to"].asString

private fun listenSearch(jsonElement: JsonElement?): String {
    val jsonObject = jsonElement as JsonObject
    return when {
        jsonObject.has("to-search") -> jsonObject["to-search"].asString
        jsonObject.has("to") -> jsonObject["to"].asString
        else -> "no-search"
    }
}

private fun listenResponse(jsonElement: JsonElement?): String = (jsonElement as JsonObject)["from"].asString


data class ProduceKey(
    val prompt: String,
    val response: String,
    val level: Long
)

data class ListenKey(
    val search: String,
    val response: String,
    val level: Long
)

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
            val params: Parameters = call.receive()
            val productionResponse = params["production_response"]
            val listenResponse = params["listen_response"]
            val clozeResponse = params["cloze_fill"]
            when {
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
    }
}
