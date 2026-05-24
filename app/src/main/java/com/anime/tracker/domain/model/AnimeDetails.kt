package com.anime.tracker.domain.model

data class AnimeDetails(
    val anime: Anime,
    val bannerImage: String?,
    val description: String?,
    val schedule: List<AiringEpisode>
)

data class AiringEpisode(
    val airingAt: Long,
    val episode: Int
)
