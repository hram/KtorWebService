package ru.hram

import io.ktor.server.application.*
import ru.hram.features.configureRouting
import ru.hram.plugins.configureContentNegotiation

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureRouting()
    configureContentNegotiation()
}
