package com.example.readflow.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.readflow.ui.theme.TextPrimary
import com.example.readflow.ui.theme.TextSecondary

@Composable
fun PlayerControlButtons(
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onRewindClick: () -> Unit,
    onForwardClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rewind 15s
        IconButton(onClick = onRewindClick) {
            Icon(
                imageVector = Icons.Rounded.Replay,
                contentDescription = "Rewind 15s",
                tint = TextPrimary,
                modifier = Modifier.size(36.dp)
            )
        }

        // Central Play/Pause
        PlayButton(
            isPlaying = isPlaying,
            onClick = onPlayPauseClick,
            size = 72
        )

        // Forward 15s
        IconButton(onClick = onForwardClick) {
            Icon(
                imageVector = Icons.Rounded.Forward10,
                contentDescription = "Forward 15s",
                tint = TextPrimary,
                modifier = Modifier.size(36.dp)
            )
        }

        // Bookmark
        IconButton(onClick = onBookmarkClick) {
            Icon(
                imageVector = Icons.Rounded.BookmarkBorder,
                contentDescription = "Bookmark",
                tint = TextSecondary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}