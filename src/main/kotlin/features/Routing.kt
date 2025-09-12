package ru.hram.features

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.hram.features.dto.Repo
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.entities.ChatId
import io.ktor.server.plugins.*
import kotlinx.coroutines.delay
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object Repos : Table("resources") {
    val name = varchar("name", 128)
    val type = varchar("type", 128)
}

fun Application.configureRouting() {
    routing {
        val telegramKey = environment.config.property("telegram.key").getString()
        val telegramGroupId = environment.config.property("telegram.group_id").getString().toLong()
        val bot = bot {
            token = telegramKey
        }
        Database.connect(
            url = environment.config.property("database.url").getString(),
            user = environment.config.property("database.user").getString(),
            password = environment.config.property("database.password").getString(),
            driver = "com.mysql.cj.jdbc.Driver",
        )

        get("/trending") {
            if (getHeaderOrThrow("token") != environment.config.property("props.auth_token").getString()) {
                throw MissingRequestParameterException("token")
            }

            val result = fetchHtml("https://github.com/trending/kotlin?since=daily")
            val repos = parseTrendingRepos(result)
            val newRepos = repos.filter { !isRepoSaved(it.toDbName()) }
            newRepos.firstOrNull()?.apply {
                bot.sendMessage(ChatId.fromId(telegramGroupId), "${this.url}\nStars: ${this.stars}")
                    .onSuccess {
                        saveRepo(this.toDbName())
                    }
            }
//            newRepos.forEach { repo ->
//                bot.sendMessage(ChatId.fromId(telegramGroupId), "${repo.url}\nStars: ${repo.stars}")
//                    .onSuccess {
//                        saveRepo(repo.toDbName())
//                    }
//                delay(5000)
//            }
            call.respond(newRepos)
        }
    }
}

fun Repo.toDbName() = "${this.owner} / ${this.name}"

fun isRepoSaved(name: String) = transaction {
    Repos.selectAll().where { Repos.name.eq(name) and Repos.type.eq("kotlin_daily") }.count() > 0
}

fun saveRepo(name: String) = transaction {
    Repos.insert {
        it[Repos.name] = name
        it[Repos.type] = "kotlin_daily"
    }
}

suspend fun fetchHtml(url: String): String {
    val client = HttpClient(OkHttp)
    return try {
        client.request(url).bodyAsText()
    } finally {
        client.close()
    }
}

fun parseTrendingRepos(html: String): List<Repo> {
    val doc: Document = Jsoup.parse(html)
    val repos = mutableListOf<Repo>()

    // Каждый репозиторий находится в article с классом Box-row
    val articles = doc.select("article.Box-row")

    for (article in articles) {
        // Название и владелец: h2.h3 a
        val linkElement = article.selectFirst("h2.h3 a") ?: continue
        val fullName = linkElement.attr("href").removePrefix("/")
        val parts = fullName.split("/")
        if (parts.size != 2) continue
        val owner = parts[0]
        val name = parts[1]

        // Описание: p.col-9
        val descriptionElement = article.selectFirst("p.col-9")
        val description = descriptionElement?.text()?.trim()

        // Звёзды: span.d-inline-block.float-sm-right
        val starsElement = article.selectFirst("span.d-inline-block.float-sm-right")
        val stars = starsElement?.text()?.trim()?.replace("\\s+".toRegex(), " ") ?: "0"

        // Язык: span[itemprop=programmingLanguage]
        val languageElement = article.selectFirst("span[itemprop=programmingLanguage]")
        val language = languageElement?.text()?.trim()

        repos.add(
            Repo(
                fullName = fullName,
                owner = owner,
                name = name,
                description = description,
                stars = stars,
                language = language,
                url = "https://github.com/$fullName"
            )
        )
    }

    return repos
}

fun RoutingContext.getHeaderOrThrow(name: String): String {
    return this.call.request.headers[name] ?: throw MissingRequestParameterException(name)
}