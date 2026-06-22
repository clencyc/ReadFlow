package com.example.readflow.repository

import android.content.Context
import com.example.readflow.model.Article
import com.example.readflow.model.FetchArticleRequest
import com.example.readflow.model.TtsRequest
import com.example.readflow.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

class ArticleRepository(private val context: Context) {

    private val api get() = RetrofitClient.getInstance(context)

    suspend fun fetchArticleFromUrl(url: String): Result<Article> = withContext(Dispatchers.IO) {
        try {
            val response = api.fetchArticle(FetchArticleRequest(url))
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Failed to fetch article: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getArticles(): Result<List<Article>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getArticles()
            if (response.isSuccessful && response.body() != null) {
                val articles = response.body()!!.articles.map { article ->
                    // Compute derived listenTime if not already set
                    article.copy(listenTime = maxOf(1, article.wordCount / 130))
                }
                Result.Success(articles)
            } else {
                Result.Error("Failed to load articles: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun deleteArticle(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteArticle(id)
            if (response.isSuccessful) Result.Success(Unit)
            else Result.Error("Delete failed: ${response.code()}")
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun generateTts(articleId: String, text: String): Result<ResponseBody> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.generateTts(TtsRequest(articleId, text))
                if (response.isSuccessful && response.body() != null) {
                    Result.Success(response.body()!!)
                } else {
                    Result.Error("TTS failed: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error(e.localizedMessage ?: "Network error")
            }
        }
}
