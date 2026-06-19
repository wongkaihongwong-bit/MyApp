package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.CompanionApp
import com.example.ui.CompanionViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: CompanionViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Check for share url parameter
    intent?.data?.let { uri ->
      val shareData = uri.getQueryParameter("share")
      if (!shareData.isNullOrEmpty()) {
        viewModel.loadSharedConversation(shareData)
      }
    }

    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = androidx.compose.ui.graphics.Color(0xFF0F172A) // Matches Slate base
        ) {
          CompanionApp(viewModel = viewModel)
        }
      }
    }
  }

  override fun onNewIntent(intent: android.content.Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    intent.data?.let { uri ->
      val shareData = uri.getQueryParameter("share")
      if (!shareData.isNullOrEmpty()) {
        viewModel.loadSharedConversation(shareData)
      }
    }
  }
}
