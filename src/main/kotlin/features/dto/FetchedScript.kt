package ru.hram.features.dto

import kotlinx.serialization.Serializable

@Serializable
data class FetchedScript(
    val status: Int,
    val statusText: String,
    val headers: Map<String, String>,
    val body: String
)
