package com.example.readflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.readflow.ui.components.PlayerControlButtons
import com.example.readflow.ui.components.SpeedSelector
import com.example.readflow.ui.theme.Background
import com.example.readflow.ui.theme.Primary
import com.example.readflow.ui.theme.SurfaceVariant
import com.example.readflow.ui.theme.TextPrimary
import com.example.readflow.ui.theme.TextSecondary
import com.example.readflow.ui.theme.TextTertiary
import com.example.readflow.ui.viewmodel.ArticleViewModel

@Composable
fun PlayerScreen(
    navController: NavController,
    viewModel: ArticleViewModel = viewModel()
) {
    val selectedArticle by viewModel.selectedArticle.observeAsState()
    val isPlaying by viewModel.isPlaying.observeAsState(false)
    val progress by viewModel.currentProgress.observeAsState(0f)
    val playbackSpeed by viewModel.playbackSpeed.observeAsState(1.0f)
    val currentPositionMs by viewModel.currentPositionMs.observeAsState(0L)
    val durationMs by viewModel.durationMs.observeAsState(0L)
    val isTtsLoading by viewModel.isTtsLoading.observeAsState(false)

    selectedArticle?.let { article ->
        Scaffold(
            containerColor = Background,
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Top Bar ──────────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = TextSecondary
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Rounded.Share,
                            contentDescription = "Share",
                            tint = TextSecondary
                        )
                    }
                }

                // ── Source Pill ──────────────────────────────────────────────────
                if (article.source.isNotBlank() || article.author.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (article.source.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .background(SurfaceVariant, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    article.source,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        if (article.author.isNotBlank()) {
                            Text(
                                article.author,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                        }
                        if (article.createdAt.isNotBlank()) {
                            Text(
                                "· ${article.createdAt.take(10)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                        }
                    }
                }

                // ── Article Title ────────────────────────────────────────────────
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                // ── Article Preview ──────────────────────────────────────────────
                val previewText = article.summary.ifBlank {
                    article.content.take(400).let { if (article.content.length > 400) "$it..." else it }
                }
                if (previewText.isNotBlank()) {
                    Text(
                        text = "❝",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = previewText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        lineHeight = MaterialTheme.typography.bodyMedium.fontSize * 1.6,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(32.dp))

                // ── TTS Loading Indicator ────────────────────────────────────────
                if (isTtsLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Primary, strokeWidth = 2.dp)
                        Text("Generating audio...", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    }
                }

                // ── Progress Bar ────────────────────────────────────────────────
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = progress.coerceIn(0f, 1f),
                        onValueChange = { viewModel.seekTo(it) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = Primary,
                            activeTrackColor = Primary,
                            inactiveTrackColor = SurfaceVariant
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            formatTime(currentPositionMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                        Text(
                            formatTime(durationMs.takeIf { it > 0 }
                                ?: (article.listenTime * 60 * 1000L)),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                // ── Playback Controls ───────────────────────────────────────────
                PlayerControlButtons(
                    isPlaying = isPlaying,
                    onPlayPauseClick = { viewModel.setPlaying(!isPlaying) },
                    onRewindClick = { viewModel.rewind15s() },
                    onForwardClick = { viewModel.forward15s() },
                    onBookmarkClick = { },
                    onMenuClick = { }
                )

                Spacer(Modifier.height(24.dp))

                // ── Speed Selector ──────────────────────────────────────────────
                SpeedSelector(
                    currentSpeed = playbackSpeed,
                    onSpeedChange = { viewModel.setPlaybackSpeed(it) }
                )

                Spacer(Modifier.height(32.dp))
            }
        }
    } ?: run {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎧", style = MaterialTheme.typography.displayMedium)
                Spacer(Modifier.height(16.dp))
                Text("No article selected", color = TextSecondary, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val secs = totalSeconds % 60
    return "%d:%02d".format(minutes, secs)
}