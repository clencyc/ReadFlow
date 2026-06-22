package com.example.readflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.readflow.ui.navigation.ReadFlowApp
import com.example.readflow.ui.theme.ReadFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReadFlowTheme(darkTheme = true) {
                ReadFlowApp()
            }
        }
    }
}
