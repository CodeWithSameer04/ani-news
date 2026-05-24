package com.anime.tracker.domain.model

data class Anime(
    val id: Int,
    val title: String,
    val imageUrl: String,
    val status: String,
    val nextEpisode: Int?,
    val airingAt: Long?, // Unix timestamp in seconds
    val categoryName: String? = null
)
