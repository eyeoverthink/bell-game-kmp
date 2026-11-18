package dev.eyeoverthink.bellgame.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.eyeoverthink.bellgame.engine.ChshConstants
import dev.eyeoverthink.bellgame.engine.MeasurementAngles
import dev.eyeoverthink.bellgame.engine.QuantumStrategy
import kotlinx.coroutines.delay
import kotlin.math.PI

/**
 * Rotate the four measurement angles and watch the win rate settle on the Born curve.
 */
@Composable
fun QuantumLabScreen(session: GameSession, modifier: Modifier = Modifier) {
    val strategy = QuantumStrategy(session.angles)
    val rate = strategy.analyticalWinRate()
    val chsh = strategy.chshValue()
    var sampling by remember { mutableStateOf(false) }
    LaunchedEffect(sampling, session.angles) {
        if (!sampling) {
            return@LaunchedEffect
        }
        session.clearLab()
        repeat(SAMPLE_BATCHES) {
            if (!sampling) {
                return@LaunchedEffect
            }
            session.recordLabRounds(ROUNDS_PER_BATCH)
            delay(BATCH_DELAY)
        }
        sampling = false
    }
    BoxWithConstraints(modifier.fillMaxSize()) {
        if (maxWidth >= 880.dp) {
            Row(
                Modifier.fillMaxSize().padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LabBody(
                    session = session,
                    rate = rate,
                    chsh = chsh,
                    sampling = sampling,
                    onSample = { sampling = true },
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                )
                LabChart(session, rate, Modifier.weight(1f))
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LabBody(
                    session = session,
                    rate = rate,
                    chsh = chsh,
                    sampling = sampling,
                    onSample = { sampling = true },
                    modifier = Modifier.fillMaxWidth(),
                )
                LabChart(session, rate, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun LabBody(
    session: GameSession,
    rate: Double,
    chsh: Double,
    sampling: Boolean,
    onSample: () -> Unit,
    modifier: Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("The bench", style = MaterialTheme.typography.headlineMedium, color = BellColors.Ink)
        Text(
            "One Bell pair. Four angles in the XZ plane. Zero points up, along Z.",
            color = BellColors.Muted,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            AngleDial(session.angles, Modifier.size(220.dp))
            Column {
                Metric(formatPercent(rate), "analytic win rate", BellColors.Ion)
                Metric(formatFixed(chsh, 3), "CHSH value S", BellColors.Brass)
                Text(benchVerdict(chsh), color = BellColors.Ink, fontSize = 14.sp)
            }
        }
        AngleSlider("Alice, x = 0", session.angles.aliceWhenZero) { degrees ->
            session.updateAngles(session.angles.copy(aliceWhenZero = degrees.asRadians()))
        }
        AngleSlider("Alice, x = 1", session.angles.aliceWhenOne) { degrees ->
            session.updateAngles(session.angles.copy(aliceWhenOne = degrees.asRadians()))
        }
        AngleSlider("Bob, y = 0", session.angles.bobWhenZero) { degrees ->
            session.updateAngles(session.angles.copy(bobWhenZero = degrees.asRadians()))
        }
        AngleSlider("Bob, y = 1", session.angles.bobWhenOne) { degrees ->
            session.updateAngles(session.angles.copy(bobWhenOne = degrees.asRadians()))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChoiceButton("Tsirelson", false, BellColors.Ion) {
                session.updateAngles(MeasurementAngles.tsirelson)
            }
            ChoiceButton("Aligned", false, BellColors.Brass) {
                session.updateAngles(MeasurementAngles.aligned)
            }
            ChoiceButton("Tilted", false, BellColors.Brass) {
                session.updateAngles(TILTED)
            }
            ChoiceButton(if (sampling) "Sampling" else "Sample 2,000", sampling, BellColors.Ion, onSample)
        }
        Text(
            "Solid needles are input 0. Dashed needles are input 1. Ion is Alice, brass is Bob.",
            color = BellColors.Muted,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun LabChart(session: GameSession, analytic: Double, modifier: Modifier) {
    Panel(modifier) {
        Text("Monte Carlo", color = BellColors.Ink)
        Text(
            "Sampled ${formatPercent(session.labLedger.winRate)} over ${session.labLedger.rounds} rounds. " +
                "The ion line is the analytic rate ${formatPercent(analytic)}.",
            color = BellColors.Muted,
            fontSize = 13.sp,
        )
        ConvergenceChart(
            samples = session.labLedger.curve,
            modifier = Modifier.fillMaxWidth().height(220.dp),
        )
        Text(
            "Ceilings: brass 75%, ion ${formatPercent(ChshConstants.tsirelsonWinRate)}, alarm 100%.",
            color = BellColors.Muted,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
        )
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun AngleSlider(label: String, radians: Double, onChange: (Float) -> Unit) {
    Column {
        Text(
            "$label   ${formatDegrees(radians)}",
            color = BellColors.Ink,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
        )
        Slider(
            value = radians.asDegrees(),
            onValueChange = onChange,
            valueRange = -180f..180f,
        )
    }
}

private fun benchVerdict(chsh: Double): String = when {
    chsh >= ChshConstants.tsirelsonChsh - 0.01 -> "Sitting on Tsirelson's bound, 2*sqrt(2)."
    chsh > ChshConstants.CLASSICAL_CHSH + 0.02 -> "Above every local strategy, under the qubit limit."
    else -> "At or under the local bound of 2. A truth table can match this."
}

private fun Double.asDegrees(): Float = (this * 180.0 / PI).toFloat()

private fun Float.asRadians(): Double = this * PI / 180.0

private val TILTED = MeasurementAngles(
    aliceWhenZero = 0.0,
    aliceWhenOne = PI / 3.0,
    bobWhenZero = PI / 6.0,
    bobWhenOne = -PI / 6.0,
)

private const val SAMPLE_BATCHES = 40
private const val ROUNDS_PER_BATCH = 50
private const val BATCH_DELAY = 16L
