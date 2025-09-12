package ru.hram.features.dto

import kotlinx.serialization.Serializable

@Serializable
data class Repo(
    val fullName: String,
    val owner: String,
    val name: String,
    val description: String?,
    val stars: String,
    val language: String?,
    val url: String,
)
