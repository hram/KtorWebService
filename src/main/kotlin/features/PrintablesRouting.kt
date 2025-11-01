package ru.hram.features

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.TelegramFile
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jsoup.Jsoup
import ru.hram.features.dto.FetchedScript
import ru.hram.features.dto.GraphResponse
import ru.hram.features.dto.ModelCard
import java.io.File

object Printables : IntIdTable("printables") {
    val name = varchar("name", 128)
}

class Printable(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Printable>(Printables)
    var name by Printables.name
}

fun Application.configurePrintablesRouting() {
    routing {
        val telegramKey = environment.config.property("telegram.key").getString()
        val telegramGroupId = environment.config.property("telegram.printables_group_id").getString().toLong()
        val bot = bot {
            token = telegramKey
        }

        post("/printables") {
            val model = call.receive<ModelCard>()
            if (!isModelSaved(model.id)) {
                val caption = "[${model.name}](${model.modelUrl})\nlikes: ${model.likes ?: 0}\ndownloads: ${model.downloads ?: 0}"
                val result = bot.sendPhoto(ChatId.fromId(telegramGroupId), TelegramFile.ByUrl(model.imageUrl), caption = caption, parseMode = ParseMode.MARKDOWN)
                if (result.first?.isSuccessful == true) {
                    saveModel(model)
                }
            }
            call.respond("OK")
        }
//        options("/printables") {
//            call.respond(HttpStatusCode.OK)
//        }
//        get("/printables3") {
//            val html = File("/home/hram/IdeaProjects/KtorWebService/src/main/kotlin/features/html.txt").readText()
//
//            val doc = Jsoup.parse(html)
//
//            val targetScript = doc.select("script[data-hash=1qez40r]").firstOrNull() ?: error("Не найден script с data-hash=\"1qez40r\"")
//
//            val json = Json { ignoreUnknownKeys = true }
//            val fetched = json.decodeFromString<FetchedScript>(targetScript.data())
//
//            val bodyAsJsonString = Json.decodeFromString<JsonObject>(targetScript.data())["body"]?.jsonPrimitive?.content ?: error("Поле 'body' отсутствует")
//
//            val graphResponse = json.decodeFromString<GraphResponse>(bodyAsJsonString)
//
//            call.respond(graphResponse.data.models.items)
//        }
//
//        get("/printables2") {
//            val html = File("/home/hram/IdeaProjects/KtorWebService/src/main/kotlin/features/html.txt").readText()
//            val models = extractModelsFromDom(html)
//            call.respond(models)
//        }
    }
}

fun isModelSaved(id: String) = transaction {
    Printable.findById(id.toInt()) != null
}

fun saveModel(model: ModelCard) = transaction {
    Printables.insert {
        it[Printables.id] = model.id.toInt()
        it[Printables.name] = model.name
    }
}

fun extractModelsFromDom(html: String): List<ModelCard> {
    val doc = Jsoup.parse(html)
    val articles = doc.select("article.card[data-testid=model]")

    return articles.mapNotNull { article ->
        // 1. Название и ссылка на модель
        val titleLink = article.selectFirst("h5 a.h.clamp-two-lines")
        val name = titleLink?.text()?.trim() ?: return@mapNotNull null
        val modelPath = titleLink.attr("href").trim()
        val modelUrl = if (modelPath.startsWith("http")) modelPath else "https://www.printables.com$modelPath"

        // 2. Ссылка на картинку
        val img = article.selectFirst("a.card-image img[alt=Image preview][class=svelte-11pdzs1]")
        val imageUrl = img?.attr("src")?.trim() ?: return@mapNotNull null

        // Пропускаем base64 placeholder
        if (imageUrl.startsWith("data:image")) return@mapNotNull null

        // 3. Количество лайков
        val likesText = article.selectFirst("span[data-testid=like-count]")?.text()?.trim()
        val likes = likesText?.replace(",", "")?.toIntOrNull() ?: 0

        // 4. Количество скачиваний
        // Ищем <i class="fa-light fa-arrow-down-to-line"> и берём следующий <span>
        val downloadIcon = article.selectFirst("i.fa-arrow-down-to-line")
        val downloadsText = downloadIcon?.parent()?.nextElementSibling()?.text()?.trim()
        val downloads = downloadsText?.replace(",", "")?.toIntOrNull() ?: 0

        ModelCard(
            id = name,
            name = name,
            modelUrl = modelUrl,
            imageUrl = imageUrl,
            likes = likes,
            downloads = downloads
        )
    }
}
