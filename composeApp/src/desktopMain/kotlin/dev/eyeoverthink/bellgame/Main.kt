package dev.eyeoverthink.bellgame

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.eyeoverthink.bellgame.ui.AppScreen
import dev.eyeoverthink.bellgame.ui.BellGameApp

/**
 * Desktop entry point.
 *
 * `BELL_SCREEN` may be `table`, `bench`, `causality`, `ledger`, or `story`.
 * `BELL_PRELOAD=1` deals a short quantum run so the score is not empty.
 * Neither variable is required.
 */
fun main() = application {
    val screen = AppScreen.fromToken(System.getenv("BELL_SCREEN"))
    val preload = System.getenv("BELL_PRELOAD") == "1"
    Window(
        onCloseRequest = ::exitApplication,
        title = "Bell Game",
        state = rememberWindowState(width = 1180.dp, height = 800.dp),
    ) {
        BellGameApp(start = screen, preloadDemo = preload)
    }
}
