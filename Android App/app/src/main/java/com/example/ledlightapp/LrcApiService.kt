package com.example.ledlightapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
////
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor


data class LrcSongResponse(
    val id: Long,
    val trackName: String?,
    val artistName: String?,
    val albumName: String?,
    val duration: Double?,
    val plainLyrics: String?,
    val syncedLyrics: String?
)


interface LrcLibWithRetrofitApi {
    @GET("search")
    suspend fun searchLyrics(
        @Query("q") query: String
    ): List<LrcSongResponse>
}

object LrcLibClient {
    private const val BASE_URL = "https://lrclib.net/api/"
    val api: LrcLibWithRetrofitApi by lazy {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.HEADERS

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request()
                    .newBuilder()
                    .header("User-Agent", "LedLightApp/1.0")
                    .build()

                chain.proceed(request)
            }
            .build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LrcLibWithRetrofitApi::class.java)
    }
}