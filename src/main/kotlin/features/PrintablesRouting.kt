package ru.hram.features

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jsoup.Jsoup
import ru.hram.features.dto.FetchedScript
import ru.hram.features.dto.GraphResponse
import ru.hram.features.dto.ModelCard
import java.io.File

fun Application.configurePrintablesRouting() {
    routing {
//        post("/ping") {
//            call.respond(HttpStatusCode.OK)
//        }
        post("/printables") {
            val request = call.receive<ModelCard>()
            call.respond(request)
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
            name = name,
            modelUrl = modelUrl,
            imageUrl = imageUrl,
            likes = likes,
            downloads = downloads
        )
    }
}
