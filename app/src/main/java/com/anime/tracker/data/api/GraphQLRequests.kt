package com.anime.tracker.data.api

data class GraphQLQuery(
    val query: String,
    val variables: Map<String, Any>
)

object AniListQueries {
    val SEARCH_AIRING_QUERY = """
        query (${'$'}search: String) {
          Page(page: 1, perPage: 25) {
            media(search: ${'$'}search, type: ANIME, format_in: [TV, TV_SHORT, ONA]) {
              id
              title { romaji english }
              coverImage { extraLarge }
              status
              nextAiringEpisode {
                airingAt
                episode
              }
            }
          }
        }
    """.trimIndent()

    val BATCH_SYNC_QUERY = """
        query (${'$'}ids: [Int]) {
          Page(page: 1, perPage: 50) {
            media(id_in: ${'$'}ids, type: ANIME) {
              id
              title { romaji english }
              coverImage { extraLarge }
              status
              nextAiringEpisode {
                airingAt
                episode
              }
            }
          }
        }
    """.trimIndent()

    val TRENDING_ANIME_QUERY = """
        query {
          Page(page: 1, perPage: 50) {
            media(status: RELEASING, sort: [TRENDING_DESC, POPULARITY_DESC], type: ANIME, isAdult: false) {
              id
              title { romaji english }
              coverImage { extraLarge }
              status
              nextAiringEpisode {
                airingAt
                episode
              }
            }
          }
        }
    """.trimIndent()

    val ANIME_DETAILS_QUERY = """
        query (${'$'}id: Int) {
          Media(id: ${'$'}id, type: ANIME) {
            id
            title { romaji english }
            coverImage { extraLarge }
            bannerImage
            description
            status
            nextAiringEpisode {
              airingAt
              episode
            }
            airingSchedule(perPage: 100) {
              nodes {
                airingAt
                episode
              }
            }
          }
        }
    """.trimIndent()
}

// Network DTO Response structures
data class AniListResponse(val data: PageData?)
data class PageData(val Page: MediaPage?, val Media: MediaDto?)
data class MediaPage(val media: List<MediaDto>?)
data class MediaDto(
    val id: Int,
    val title: TitleDto?,
    val coverImage: CoverImageDto?,
    val bannerImage: String?,
    val description: String?,
    val status: String?,
    val nextAiringEpisode: NextEpisodeDto?,
    val airingSchedule: AiringScheduleDto?
)
data class TitleDto(val romaji: String?, val english: String?)
data class CoverImageDto(val extraLarge: String?)
data class NextEpisodeDto(val airingAt: Long?, val episode: Int?)
data class AiringScheduleDto(val nodes: List<NextEpisodeDto>?)
