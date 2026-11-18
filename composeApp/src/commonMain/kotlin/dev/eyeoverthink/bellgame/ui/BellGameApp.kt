package dev.eyeoverthink.bellgame.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.eyeoverthink.bellgame.engine.Regime

/**
 * The whole exhibit. Every screen is shared; the platform shells only call this.
 *
 * [preloadDemo] deals a short quantum run so a cold start can show a live score.
 */
@Composable
fun BellGameApp(start: AppScreen = AppScreen.Table, preloadDemo: Boolean = false) {
    val session = remember { GameSession() }
    var screen by remember { mutableStateOf(start) }
    LaunchedEffect(preloadDemo) {
        if (preloadDemo) {
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
                icon = { Text(if (screen == entry) "●" else "○", color = BellColors.Ion) },
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
                icon = { Text(if (screen == entry) "●" else "○", color = BellColors.Ion, fontSize = 12.sp) },
                label = { Text(entry.label, fontSize = 11.sp) },
            )
        }
    }
}

private const val PRELOAD_ROUNDS = 48
