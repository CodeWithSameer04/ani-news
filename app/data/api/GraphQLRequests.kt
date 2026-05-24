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
}

// Network DTO Response structures
data class AniListResponse(val data: PageData?)
data class PageData(val Page: MediaPage?)
data class MediaPage(val media: List<MediaDto>?)
data class MediaDto(
    val id: Int,
    val title: TitleDto?,
    val coverImage: CoverImageDto?,
    val status: String?,
    val nextAiringEpisode: NextEpisodeDto?
)
data class TitleDto(val romaji: String?, val english: String?)
data class CoverImageDto(val extraLarge: String?)
data class NextEpisodeDto(val airingAt: Long?, val episode: Int?)