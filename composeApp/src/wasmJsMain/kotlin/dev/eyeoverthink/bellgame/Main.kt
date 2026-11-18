package dev.eyeoverthink.bellgame

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.eyeoverthink.bellgame.ui.AppScreen
import dev.eyeoverthink.bellgame.ui.BellGameApp
import kotlinx.browser.document
import kotlinx.browser.window

/**
 * Browser entry point. A URL hash such as `#bench` opens that screen.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val body = document.body ?: error("Bell Game needs a document body.")
    val screen = AppScreen.fromToken(window.location.hash.removePrefix("#"))
    ComposeViewport(body) {
        BellGameApp(start = screen)
    }
}
