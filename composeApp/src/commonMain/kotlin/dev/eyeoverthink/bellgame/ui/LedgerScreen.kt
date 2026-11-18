package dev.eyeoverthink.bellgame.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.eyeoverthink.bellgame.engine.Ledger

/**
 * The score from the table and the score from the bench, side by side.
 */
@Composable
fun LedgerScreen(session: GameSession, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("The ledger", style = MaterialTheme.typography.headlineMedium, color = BellColors.Ink)
        Text(
            "Brass is 75%, ion is cos^2(pi/8), alarm is a perfect score. " +
                "A long enough run sits on the line its strategy can actually reach.",
            color = BellColors.Muted,
        )
        LedgerCard(
            title = "Table - ${session.regime.title}",
            ledger = session.ledger,
        )
        LedgerCard(
            title = "Bench sample",
            ledger = session.labLedger,
        )
    }
}

@Composable
private fun LedgerCard(title: String, ledger: Ledger) {
    Panel {
        Text(title, color = BellColors.Ink, style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Metric(formatPercent(ledger.winRate), "win rate", BellColors.Ink)
            Metric(ledger.wins.toString(), "wins", BellColors.Win)
            Metric(ledger.rounds.toString(), "rounds", BellColors.Muted)
        }
        ConvergenceChart(
            samples = ledger.curve,
            modifier = Modifier.fillMaxWidth().height(140.dp),
        )
        Text("By input pair", color = BellColors.Muted, fontSize = 12.sp)
        for (x in 0..1) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                for (y in 0..1) {
                    val dealt = ledger.roundsFor(x, y)
                    val label = if (dealt == 0) {
                        "x$x y$y  -"
                    } else {
                        "x$x y$y  ${formatPercent(ledger.rateFor(x, y))}  ($dealt)"
                    }
                    Text(
                        text = label,
                        color = BellColors.Ink,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
