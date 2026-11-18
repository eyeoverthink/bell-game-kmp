package dev.eyeoverthink.bellgame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.eyeoverthink.bellgame.engine.ChshConstants
import dev.eyeoverthink.bellgame.engine.LocalTable
import dev.eyeoverthink.bellgame.engine.QuantumStrategy
import dev.eyeoverthink.bellgame.engine.Regime
import dev.eyeoverthink.bellgame.engine.Round
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * The playable table. The user commits a strategy, then the referee deals bits.
 */
@Composable
fun PlayScreen(session: GameSession, modifier: Modifier = Modifier) {
    LaunchedEffect(session.running, session.pace) {
        if (!session.running) {
            return@LaunchedEffect
        }
        while (isActive) {
            session.playOne()
            delay(session.pace.delayMillis)
        }
    }
    BoxWithConstraints(modifier.fillMaxSize()) {
        val wide = maxWidth >= 880.dp
        if (wide) {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                StrategyColumn(session, Modifier.weight(1f).verticalScroll(rememberScrollState()))
                StageColumn(session, Modifier.weight(1.15f).verticalScroll(rememberScrollState()))
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                StrategyColumn(session, Modifier.fillMaxWidth())
                StageColumn(session, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun StrategyColumn(session: GameSession, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("The table", style = MaterialTheme.typography.headlineMedium, color = BellColors.Ink)
        Text(
            "Alice gets x. Bob gets y. They answer with a and b. " +
                "They win when a XOR b equals x AND y.",
            color = BellColors.Muted,
        )
        RegimePicker(session.regime, session::selectRegime)
        when (session.regime) {
            Regime.Classical -> TableEditor(session)
            Regime.Quantum -> QuantumNote(session)
            Regime.PopescuRohrlich -> PrNote()
        }
        ControlRow(session)
    }
}

@Composable
private fun StageColumn(session: GameSession, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        RoundStage(session.lastRound, session.notice)
        ScoreCard(session)
    }
}

@Composable
private fun RegimePicker(selected: Regime, onSelect: (Regime) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Regime.entries.forEach { regime ->
            ChoiceButton(
                text = regime.title,
                selected = regime == selected,
                color = regimeColor(regime),
                onClick = { onSelect(regime) },
            )
        }
    }
}

@Composable
private fun TableEditor(session: GameSession) {
    Panel {
        Text("Local truth table", color = BellColors.Ink)
        Text(
            "Each switch is committed before the bits are dealt. " +
                "No setting wins all four deals. The best is 3 of 4.",
            color = BellColors.Muted,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(8.dp))
        BitChoice("Alice if x = 0", session.table.aliceWhenZero) {
            session.updateTable(session.table.copy(aliceWhenZero = it))
        }
        BitChoice("Alice if x = 1", session.table.aliceWhenOne) {
            session.updateTable(session.table.copy(aliceWhenOne = it))
        }
        BitChoice("Bob if y = 0", session.table.bobWhenZero) {
            session.updateTable(session.table.copy(bobWhenZero = it))
        }
        BitChoice("Bob if y = 1", session.table.bobWhenOne) {
            session.updateTable(session.table.copy(bobWhenOne = it))
        }
        Spacer(Modifier.height(8.dp))
        TruthGrid(session.table)
        Text(
            "Locked-in win rate ${formatPercent(session.table.exactWinRate())}",
            color = BellColors.Brass,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun QuantumNote(session: GameSession) {
    val rate = QuantumStrategy(session.angles).analyticalWinRate()
    Panel {
        Text("Shared Bell pair", color = BellColors.Ink)
        Text(
            "Alice measures at ${formatDegrees(session.angles.aliceWhenZero)} and " +
                "${formatDegrees(session.angles.aliceWhenOne)}. " +
                "Bob measures at ${formatDegrees(session.angles.bobWhenZero)} and " +
                "${formatDegrees(session.angles.bobWhenOne)}. Bend these on the bench.",
            color = BellColors.Muted,
            fontSize = 13.sp,
        )
        Text(
            "Analytic win rate ${formatPercent(rate)}",
            color = BellColors.Ion,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun PrNote() {
    Panel {
        Text("Not a physical strategy", color = BellColors.Alarm)
        Text(
            "The PR box wins every deal and still refuses to signal. " +
                "The causality screen shows what that perfection would leak.",
            color = BellColors.Muted,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun TruthGrid(table: LocalTable) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (x in 0..1) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (y in 0..1) {
                    val won = table.winsInput(x, y)
                    Text(
                        text = "x$x y$y  ${if (won) "win" else "miss"}",
                        color = if (won) BellColors.Win else BellColors.Miss,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        modifier = Modifier.width(120.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RoundStage(round: Round?, notice: String) {
    Panel {
        Text("This deal", color = BellColors.Muted, fontSize = 12.sp)
        if (round == null) {
            Text(
                "Nothing dealt yet.",
                color = BellColors.Ink,
                style = MaterialTheme.typography.headlineSmall,
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BitToken("x", round.x, emphasize = false)
                BitToken("y", round.y, emphasize = false)
                BitToken("a", round.a, emphasize = true)
                BitToken("b", round.b, emphasize = true)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (round.won) "WIN" else "MISS",
                color = if (round.won) BellColors.Win else BellColors.Miss,
                style = MaterialTheme.typography.headlineMedium,
            )
        }
        Text(notice, color = BellColors.Ink, fontSize = 14.sp)
    }
}

@Composable
private fun ScoreCard(session: GameSession) {
    Panel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Metric(formatPercent(session.ledger.winRate), "running win rate", BellColors.Ink)
            Metric(session.ledger.rounds.toString(), "rounds", BellColors.Muted)
        }
        CeilingGauge(session.ledger.winRate, session.ledger.rounds)
        Text(banner(session), color = regimeColor(session.regime), fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        ConvergenceChart(
            samples = session.ledger.curve,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
        )
    }
}

@Composable
private fun ControlRow(session: GameSession) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChoiceButton("Step", selected = false, color = BellColors.Ion, onClick = {
                session.running = false
                session.playOne()
            })
            ChoiceButton(
                text = if (session.running) "Pause" else "Run",
                selected = session.running,
                color = BellColors.Ion,
                onClick = { session.running = !session.running },
            )
            ChoiceButton("Reset", selected = false, color = BellColors.Brass, onClick = session::resetTable)
            ChoiceButton("New seed", selected = false, color = BellColors.Brass, onClick = session::reseed)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Pace", color = BellColors.Muted)
            Pace.entries.forEach { pace ->
                ChoiceButton(
                    text = pace.label,
                    selected = session.pace == pace,
                    color = BellColors.Ion,
                    onClick = { session.pace = pace },
                )
            }
        }
        Text("Seed ${session.seed}", color = BellColors.Muted, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
    }
}

@Composable
private fun BitChoice(label: String, value: Int, onSelect: (Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 2.dp),
    ) {
        Text(label, color = BellColors.Ink, modifier = Modifier.width(150.dp), fontSize = 14.sp)
        ChoiceButton("0", value == 0, BellColors.Brass) { onSelect(0) }
        ChoiceButton("1", value == 1, BellColors.Brass) { onSelect(1) }
    }
}

@Composable
internal fun ChoiceButton(text: String, selected: Boolean, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) color else BellColors.Line,
            contentColor = if (selected) BellColors.Night else BellColors.Ink,
        ),
    ) {
        Text(text)
    }
}

@Composable
internal fun Panel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .background(BellColors.Panel, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        content = { content() },
    )
}

private fun banner(session: GameSession): String {
    val rounds = session.ledger.rounds
    if (rounds < 20) {
        return "Twenty rounds is enough for the average to start telling the truth."
    }
    return when (session.regime) {
        Regime.Classical -> {
            val exact = session.table.exactWinRate()
            "This table is worth ${formatPercent(exact)} exactly. Sampling should settle there."
        }

        Regime.Quantum -> {
            val target = QuantumStrategy(session.angles).analyticalWinRate()
            if (session.ledger.winRate > ChshConstants.CLASSICAL_WIN_RATE + 0.02) {
                "Local ceiling cleared. The curve for these angles is ${formatPercent(target)}."
            } else {
                "Still near the local ceiling. The quantum curve for these angles is ${formatPercent(target)}."
            }
        }

        Regime.PopescuRohrlich -> "Every deal is a win. Open Causality to see the leak."
    }
}
