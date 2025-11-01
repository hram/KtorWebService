package ru.hram.features.dto

import kotlinx.serialization.Serializable

@Serializable
data class ModelCard(
    val id: String,
    val name: String,
    val modelUrl: String,
    val imageUrl: String,
    val likes: Int?,
    val downloads: Int?
)