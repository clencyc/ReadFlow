package com.example.readflow

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.readflow.ui.theme.Background
import com.example.readflow.ui.theme.CardBackground
import com.example.readflow.ui.theme.Primary
import com.example.readflow.ui.theme.ReadFlowTheme
import com.example.readflow.ui.theme.TextPrimary
import com.example.readflow.ui.theme.TextSecondary
import com.example.readflow.ui.theme.TextTertiary
import com.example.readflow.ui.viewmodel.ArticleViewModel

class ShareReceiverActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedText = intent?.getStringExtra(Intent.EXTRA_TEXT) ?: ""
        val url = extractUrl(sharedText)

        if (url == null) {
            Toast.makeText(this, "No URL found in shared text", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            ReadFlowTheme(darkTheme = true) {
                ShareConfirmationSheet(
                    url = url,
                    onDismiss = { finish() },
                    onOpenApp = {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }

    private fun extractUrl(text: String): String? {
        val urlRegex = Regex("""https?://[^\s]+""")
        return urlRegex.find(text)?.value
    }
}

@Composable
fun ShareConfirmationSheet(
    url: String,
    onDismiss: () -> Unit,
    onOpenApp: () -> Unit
) {
    val viewModel: ArticleViewModel = viewModel()
    val isFetching by viewModel.isFetchingArticle.observeAsState(false)
    var added by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.addArticleFromUrl(
            url = url,
            onSuccess = { added = true },
            onError = { errorMsg = it }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background.copy(alpha = 0.85f)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when {
                    isFetching -> {
                        CircularProgressIndicator(color = Primary, modifier = Modifier.size(32.dp))
                        Text("Fetching article...", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            url.take(50) + if (url.length > 50) "..." else "",
                            color = TextTertiary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    added -> {
                        Text("✅", style = MaterialTheme.typography.displaySmall)
                        Text(
                            "Added to Queue!",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "Article saved to your ReadFlow queue.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TextButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Done", color = TextSecondary)
                            }
                            Button(
                                onClick = onOpenApp,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = TextPrimary)
                            ) {
                                Text("Open App")
                            }
                        }
                    }
                    errorMsg != null -> {
                        Text("⚠️", style = MaterialTheme.typography.displaySmall)
                        Text("Couldn't add article", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(errorMsg ?: "", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = TextPrimary)
                        ) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }
    }
}
