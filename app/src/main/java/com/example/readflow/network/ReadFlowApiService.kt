package com.example.readflow.network

import com.example.readflow.model.Article
import com.example.readflow.model.ArticleListResponse
import com.example.readflow.model.FetchArticleRequest
import com.example.readflow.model.TtsRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming

interface ReadFlowApiService {

    /** Fetch & parse an article from a URL, save to server queue */
    @POST("api/articles/fetch")
    suspend fun fetchArticle(@Body request: FetchArticleRequest): Response<Article>

    /** Get all saved articles */
    @GET("api/articles")
    suspend fun getArticles(): Response<ArticleListResponse>

    /** Get single article by ID */
    @GET("api/articles/{id}")
    suspend fun getArticle(@Path("id") id: String): Response<Article>

    /** Delete article from queue */
    @DELETE("api/articles/{id}")
    suspend fun deleteArticle(@Path("id") id: String): Response<Unit>

    /** Generate TTS audio — returns raw MP3 bytes */
    @Streaming
    @POST("api/tts/generate")
    suspend fun generateTts(@Body request: TtsRequest): Response<ResponseBody>
}
