package com.anime.tracker.data.api

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AniListApiService {
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("/")
    suspend fun postGraphQL(@Body body: GraphQLQuery): AniListResponse
}
