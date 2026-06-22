package com.example.readflow.model

import com.google.gson.annotations.SerializedName

data class Article(
    @SerializedName("id")         val id: String = "",
    @SerializedName("url")        val url: String = "",
    @SerializedName("title")      val title: String = "",
    @SerializedName("content")    val content: String = "",
    @SerializedName("summary")    val summary: String = "",
    @SerializedName("author")     val author: String = "",
    @SerializedName("source")     val source: String = "",
    @SerializedName("image_url")  val imageUrl: String? = null,
    @SerializedName("word_count") val wordCount: Int = 0,
    @SerializedName("created_at") val createdAt: String = "",
    // Derived field: estimated listen time in minutes (avg 130 words/min for TTS)
    val listenTime: Int = maxOf(1, wordCount / 130),
    val isPlaying: Boolean = false,
    val progressFraction: Float = 0f
)

// API request / response wrappers
data class FetchArticleRequest(
    @SerializedName("url") val url: String
)

data class TtsRequest(
    @SerializedName("article_id") val articleId: String,
    @SerializedName("text")       val text: String
)

data class ArticleListResponse(
    @SerializedName("articles") val articles: List<Article> = emptyList()
)