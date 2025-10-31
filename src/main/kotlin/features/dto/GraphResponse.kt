package ru.hram.features.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GraphResponse(
    val data:  ModelsData,
) {
    @Serializable
    data class ModelsData(
        val models: ModelContainer
    )

    @Serializable
    data class ModelContainer(
        val cursor: String,
        val items: List<ModelItem>
    )

    @Serializable
    data class ModelItem(
        val id: String,
        val name: String,
        val slug: String,
        val ratingAvg: String,
        val likesCount: Long,
        val liked: Boolean,
        val datePublished: String,
        val dateFeatured: String?,
        val firstPublish: String,
        val downloadCount: Long,
        val mmu: Boolean,
        val category: Category,
        val modified: String,
        val image: Image,
        val imagesCount: Long,
        val nsfw: Boolean,
        val aiGenerated: Boolean,
        val club: Boolean,
        val user: User,
        @SerialName("__typename")
        val typename: String,
    )

    @Serializable
    data class Category(
        val id: String,
        val path: List<Path>,
        @SerialName("__typename")
        val typename: String,
    )

    @Serializable
    data class Path(
        val id: String,
        val name: String,
        val nameEn: String,
        @SerialName("__typename")
        val typename: String,
    )

    @Serializable
    data class Image(
        val id: String,
        val filePath: String,
        val rotation: Long,
        val imageHash: String,
        val imageWidth: Long,
        val imageHeight: Long,
        @SerialName("__typename")
        val typename: String,
    )

    @Serializable
    data class User(
        val id: String,
        val handle: String,
        val verified: Boolean,
        val dateVerified: String?,
        val publicUsername: String,
        val avatarFilePath: String,
        val badgesProfileLevel: BadgesProfileLevel,
        @SerialName("__typename")
        val typename: String,
        val isHiddenForMe: Boolean,
        val level: Long,
        val avatar: String,
    )

    @Serializable
    data class BadgesProfileLevel(
        val profileLevel: Long,
        @SerialName("__typename")
        val typename: String,
    )
}
