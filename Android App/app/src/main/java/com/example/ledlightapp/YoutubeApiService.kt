package com.example.ledlightapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


data class YouTubeResponse(
    val items: List<YouTubeVideoItem>
)

data class YouTubeVideoItem(
    val snippet: VideoSnippet,
    val contentDetails: ContentDetails
)

data class VideoSnippet(
    val title: String
)

data class ContentDetails(
    val duration: String
)


interface YouTubeWithRetrofitApi {

    @GET("youtube/v3/videos")
    suspend fun getVideoDetails(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("id") videoId: String,
        @Query("key") apiKey: String
    ): YouTubeResponse
}


object YouTubeClient {

    private const val BASE_URL = "https://www.googleapis.com/"

    val api: YouTubeWithRetrofitApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(YouTubeWithRetrofitApi::class.java)
    }
}