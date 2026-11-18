package dev.eyeoverthink.bellgame.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.eyeoverthink.bellgame.engine.Regime

/**
 * The whole exhibit. Every screen is shared; the platform shells only call this.
 *
 * [preloadDemo] deals a short quantum run into the score that [start] actually
 * shows. The bench has its own ledger, so opening the bench fills that sample.
 * Every other screen fills the table.
 */
@Composable
fun BellGameApp(start: AppScreen = AppScreen.Table, preloadDemo: Boolean = false) {
    val session = remember { GameSession() }
    var screen by remember { mutableStateOf(start) }
    LaunchedEffect(preloadDemo, start) {
        if (!preloadDemo) {
            return@LaunchedEffect
        }
        if (preloadFillsBench(start)) {
            session.recordLabRounds(PRELOAD_ROUNDS)
        } else {
            session.selectRegime(Regime.Quantum)
            repeat(PRELOAD_ROUNDS) {
                session.playOne()
            }
        }
    }
    BellTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = BellColors.Night) {
            BoxWithConstraints(
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
            ) {
                if (maxWidth >= 900.dp) {
                    Row(Modifier.fillMaxSize()) {
                        ExhibitRail(screen) { screen = it }
                        Column(Modifier.weight(1f).fillMaxHeight()) {
                            Wordmark()
                            ScreenBody(screen, session, Modifier.weight(1f))
                        }
                    }
                } else {
                    Column(Modifier.fillMaxSize()) {
                        Wordmark()
                        ScreenBody(screen, session, Modifier.weight(1f))
                        ExhibitBar(screen) { screen = it }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenBody(screen: AppScreen, session: GameSession, modifier: Modifier) {
    Box(modifier) {
        when (screen) {
            AppScreen.Table -> PlayScreen(session, Modifier.fillMaxSize())
            AppScreen.Bench -> QuantumLabScreen(session, Modifier.fillMaxSize())
            AppScreen.Causality -> CausalityScreen(Modifier.fillMaxSize())
            AppScreen.Ledger -> LedgerScreen(session, Modifier.fillMaxSize())
            AppScreen.Story -> StoryScreen(Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun Wordmark() {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text("BELL GAME", color = BellColors.Ion, fontSize = 20.sp)
        Text(
            "Beat the CHSH game, up to the line nature actually draws.",
            color = BellColors.Muted,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun ExhibitRail(screen: AppScreen, onSelect: (AppScreen) -> Unit) {
    NavigationRail(
        modifier = Modifier.width(148.dp).fillMaxHeight(),
        containerColor = BellColors.Panel,
    ) {
        AppScreen.entries.forEach { entry ->
            NavigationRailItem(
                selected = screen == entry,
                onClick = { onSelect(entry) },
                icon = { NavMark(selected = screen == entry) },
                label = { Text(entry.label) },
            )
        }
    }
}

@Composable
private fun ExhibitBar(screen: AppScreen, onSelect: (AppScreen) -> Unit) {
    NavigationBar(containerColor = BellColors.Panel) {
        AppScreen.entries.forEach { entry ->
            NavigationBarItem(
                selected = screen == entry,
                onClick = { onSelect(entry) },
                icon = { NavMark(selected = screen == entry) },
                label = { Text(entry.label, fontSize = 11.sp) },
            )
        }
    }
}

/**
 * A filled or open circle drawn by the app.
 *
 * These marks used to be the text glyphs ● and ○. On Wasm, Skia downloads a
 * Noto fallback from fonts.gstatic.com for any code point the embedded face
 * does not contain. A circle does not ask for that face.
 */
@Composable
private fun NavMark(selected: Boolean) {
    Canvas(Modifier.size(12.dp)) {
        if (selected) {
            drawCircle(color = BellColors.Ion)
        } else {
            drawCircle(
                color = BellColors.Ion,
                style = Stroke(width = 1.5.dp.toPx()),
            )
        }
    }
}

/**
 * Whether a cold-start preload should fill the bench sample instead of the table.
 */
fun preloadFillsBench(screen: AppScreen): Boolean = screen == AppScreen.Bench

private const val PRELOAD_ROUNDS = 48
