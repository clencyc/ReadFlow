package com.example.readflow.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.readflow.model.Article
import com.example.readflow.repository.ArticleRepository
import com.example.readflow.repository.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ArticleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ArticleRepository(application)

    // ─── Articles List ──────────────────────────────────────────────────────────
    private val _articles = MutableLiveData<List<Article>>(emptyList())
    val articles: LiveData<List<Article>> = _articles

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isFetchingArticle = MutableLiveData(false)
    val isFetchingArticle: LiveData<Boolean> = _isFetchingArticle

    // ─── Selected Article / Player ──────────────────────────────────────────────
    private val _selectedArticle = MutableLiveData<Article?>(null)
    val selectedArticle: LiveData<Article?> = _selectedArticle

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _currentProgress = MutableLiveData(0f)
    val currentProgress: LiveData<Float> = _currentProgress

    private val _playbackSpeed = MutableLiveData(1.0f)
    val playbackSpeed: LiveData<Float> = _playbackSpeed

    private val _isTtsLoading = MutableLiveData(false)
    val isTtsLoading: LiveData<Boolean> = _isTtsLoading

    private val _currentPositionMs = MutableLiveData(0L)
    val currentPositionMs: LiveData<Long> = _currentPositionMs

    private val _durationMs = MutableLiveData(0L)
    val durationMs: LiveData<Long> = _durationMs

    // ─── ExoPlayer ──────────────────────────────────────────────────────────────
    private var exoPlayer: ExoPlayer? = null
    private var progressJob: Job? = null
    private var currentAudioFile: File? = null

    init {
        loadArticles()
    }

    // ─── Load articles from API ─────────────────────────────────────────────────
    fun loadArticles() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = repository.getArticles()) {
                is Result.Success -> {
                    _articles.value = result.data
                    _errorMessage.value = null
                }
                is Result.Error   -> _errorMessage.value = result.message
                is Result.Loading -> { /* handled by _isLoading */ }
            }
            _isLoading.value = false
        }
    }

    // ─── Add article from URL ───────────────────────────────────────────────────
    fun addArticleFromUrl(url: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isFetchingArticle.value = true
            when (val result = repository.fetchArticleFromUrl(url)) {
                is Result.Success -> {
                    val current = _articles.value.orEmpty().toMutableList()
                    current.add(0, result.data)
                    _articles.value = current
                    _errorMessage.value = null
                    onSuccess()
                }
                is Result.Error -> {
                    _errorMessage.value = result.message
                    onError(result.message)
                }
                is Result.Loading -> {}
            }
            _isFetchingArticle.value = false
        }
    }

    // ─── Delete article ─────────────────────────────────────────────────────────
    fun deleteArticle(article: Article) {
        viewModelScope.launch {
            when (repository.deleteArticle(article.id)) {
                is Result.Success -> {
                    _articles.value = _articles.value.orEmpty().filter { it.id != article.id }
                    if (_selectedArticle.value?.id == article.id) {
                        stopPlayback()
                        _selectedArticle.value = null
                    }
                }
                is Result.Error, is Result.Loading -> {}
            }
        }
    }

    // ─── Select article & load TTS ──────────────────────────────────────────────
    fun selectArticle(article: Article) {
        if (_selectedArticle.value?.id == article.id) return
        stopPlayback()
        _selectedArticle.value = article
        _currentProgress.value = 0f
        _currentPositionMs.value = 0L
        _durationMs.value = (article.listenTime * 60 * 1000).toLong()
    }

    // ─── Search ─────────────────────────────────────────────────────────────────
    fun searchArticles(query: String): List<Article> {
        return _articles.value.orEmpty().filter { article ->
            article.title.contains(query, ignoreCase = true) ||
                    article.author.contains(query, ignoreCase = true) ||
                    article.source.contains(query, ignoreCase = true)
        }
    }

    // ─── Playback controls ──────────────────────────────────────────────────────
    fun setPlaying(playing: Boolean) {
        val article = _selectedArticle.value ?: return
        if (playing) {
            if (exoPlayer == null || exoPlayer?.isPlaying == false) {
                loadAndPlayTts(article)
            } else {
                exoPlayer?.play()
                _isPlaying.value = true
                startProgressTracking()
            }
        } else {
            exoPlayer?.pause()
            _isPlaying.value = false
            progressJob?.cancel()
        }
    }

    fun seekTo(fraction: Float) {
        val duration = exoPlayer?.duration ?: return
        if (duration > 0) {
            val positionMs = (fraction * duration).toLong()
            exoPlayer?.seekTo(positionMs)
            _currentProgress.value = fraction
            _currentPositionMs.value = positionMs
        } else {
            _currentProgress.value = fraction
        }
    }

    fun rewind15s() {
        val current = exoPlayer?.currentPosition ?: 0L
        val newPos = (current - 15_000).coerceAtLeast(0L)
        exoPlayer?.seekTo(newPos)
        val duration = exoPlayer?.duration?.takeIf { it > 0 } ?: 1L
        _currentPositionMs.value = newPos
        _currentProgress.value = newPos.toFloat() / duration.toFloat()
    }

    fun forward15s() {
        val current = exoPlayer?.currentPosition ?: 0L
        val duration = exoPlayer?.duration ?: 0L
        val newPos = (current + 15_000).coerceAtMost(if (duration > 0) duration else current + 15_000)
        exoPlayer?.seekTo(newPos)
        if (duration > 0) {
            _currentPositionMs.value = newPos
            _currentProgress.value = newPos.toFloat() / duration.toFloat()
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        exoPlayer?.setPlaybackSpeed(speed)
    }

    fun setProgress(fraction: Float) {
        seekTo(fraction)
    }

    // ─── TTS loading + ExoPlayer setup ─────────────────────────────────────────
    private fun loadAndPlayTts(article: Article) {
        viewModelScope.launch {
            _isTtsLoading.value = true
            val textToRead = if (article.content.isNotBlank()) article.content else article.summary
            val chunk = textToRead.take(1500) // API may have size limits

            when (val result = repository.generateTts(article.id, chunk)) {
                is Result.Success -> {
                    val audioFile = saveAudioToTemp(result.data, article.id)
                    if (audioFile != null) {
                        currentAudioFile = audioFile
                        initAndPlayExoPlayer(audioFile)
                    } else {
                        _errorMessage.value = "Failed to save audio"
                    }
                }
                is Result.Error -> {
                    _errorMessage.value = "TTS Error: ${result.message}"
                }
                is Result.Loading -> {}
            }
            _isTtsLoading.value = false
        }
    }

    private suspend fun saveAudioToTemp(body: okhttp3.ResponseBody, articleId: String): File? =
        withContext(Dispatchers.IO) {
            try {
                val dir = getApplication<Application>().cacheDir
                val file = File(dir, "tts_$articleId.mp3")
                body.byteStream().use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                file
            } catch (e: Exception) {
                null
            }
        }

    private fun initAndPlayExoPlayer(file: File) {
        val context: Context = getApplication()
        exoPlayer?.release()
        exoPlayer = ExoPlayer.Builder(context).build().also { player ->
            player.setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
            player.playbackParameters = androidx.media3.common.PlaybackParameters(_playbackSpeed.value ?: 1.0f)
            player.prepare()
            player.play()
            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_READY   -> _durationMs.value = player.duration
                        Player.STATE_ENDED   -> {
                            _isPlaying.value = false
                            _currentProgress.value = 1f
                            progressJob?.cancel()
                        }
                        else -> {}
                    }
                }
                override fun onIsPlayingChanged(playing: Boolean) {
                    _isPlaying.value = playing
                }
            })
        }
        _isPlaying.value = true
        startProgressTracking()
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                delay(500)
                val player = exoPlayer ?: break
                val duration = player.duration.takeIf { it > 0 } ?: continue
                val position = player.currentPosition
                _currentPositionMs.value = position
                _currentProgress.value = position.toFloat() / duration.toFloat()
            }
        }
    }

    private fun stopPlayback() {
        progressJob?.cancel()
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
        _isPlaying.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
    }
}