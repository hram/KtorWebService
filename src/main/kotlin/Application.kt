package ru.hram

import io.ktor.server.application.*
import ru.hram.features.configureDatabase
import ru.hram.features.configurePingRouting
import ru.hram.features.configurePrintablesRouting
import ru.hram.features.configureTrendingRouting
import ru.hram.plugins.configureContentNegotiation
import ru.hram.plugins.configureCors

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configurePingRouting()
    configureDatabase()
    configureTrendingRouting()
    configurePrintablesRouting()
    configureContentNegotiation()
    configureCors()
}
