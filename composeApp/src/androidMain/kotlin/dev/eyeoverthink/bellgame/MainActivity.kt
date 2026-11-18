package dev.eyeoverthink.bellgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.eyeoverthink.bellgame.ui.BellGameApp

/**
 * Android entry point. The screen itself lives in common code.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BellGameApp()
        }
    }
}
