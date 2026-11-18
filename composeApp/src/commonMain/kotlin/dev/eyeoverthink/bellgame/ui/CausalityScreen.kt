package dev.eyeoverthink.bellgame.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.eyeoverthink.bellgame.engine.AccessHand
import dev.eyeoverthink.bellgame.engine.ChshConstants
import dev.eyeoverthink.bellgame.engine.InformationCausality
import dev.eyeoverthink.bellgame.engine.NestedRandomAccess
import dev.eyeoverthink.bellgame.engine.Regime
import dev.eyeoverthink.bellgame.engine.SeededRandom
import kotlin.math.roundToInt

/**
 * One classical bit from Alice, and a count of how many bits Bob can reach.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CausalityScreen(modifier: Modifier = Modifier) {
    var layers by remember { mutableIntStateOf(2) }
    var regime by remember { mutableStateOf(Regime.PopescuRohrlich) }
    val correlation = correlationOf(regime)
    val protocol = remember(layers, correlation) { NestedRandomAccess(layers, correlation) }
    val random = remember { SeededRandom(7) }
    var hand by remember { mutableStateOf<AccessHand?>(null) }
    var sampled by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val info = InformationCausality.accessibleBits(correlation, layers)
    val theory = InformationCausality.successProbability(correlation, layers)
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("One bit, too many", style = MaterialTheme.typography.headlineMedium, color = BellColors.Ink)
        Text(
            "Alice holds a string. She may send one bit. Bob points at any bit in the string " +
                "and tries to name it. Information causality says the information he gains, " +
                "added up over the string, cannot exceed that one bit.",
            color = BellColors.Muted,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Regime.entries.forEach { entry ->
                ChoiceButton(entry.title, entry == regime, regimeColor(entry)) {
                    regime = entry
                    hand = null
                    sampled = null
                }
            }
        }
        Text(
            "Nesting depth $layers, so Alice holds ${protocol.bitCount} bits. " +
                "Correlation E = ${formatFixed(correlation, 4)}. CHSH S = ${formatFixed(4.0 * correlation, 3)}.",
            color = BellColors.Ink,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
        )
        Slider(
            value = layers.toFloat(),
            onValueChange = {
                layers = it.roundToInt().coerceIn(1, 5)
                hand = null
                sampled = null
            },
            valueRange = 1f..5f,
            steps = 3,
        )
        InfoMeter(info)
        Text(causalityVerdict(info), color = if (info > 1.0) BellColors.Alarm else BellColors.Ion)
        Text(
            "Closed-form success ${formatPercent(theory)} on the bit Bob asks for.",
            color = BellColors.Muted,
            fontSize = 13.sp,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChoiceButton("Deal one hand", false, BellColors.Ion) {
                hand = deal(protocol, random)
            }
            ChoiceButton("Run 400 hands", false, BellColors.Brass) {
                sampled = runBatch(protocol, random, BATCH)
            }
        }
        hand?.let { dealt -> HandCard(dealt) }
        sampled?.let { (correct, trials) ->
            Text(
                "Sampled ${formatPercent(correct.toDouble() / trials)} over $trials hands.",
                color = BellColors.Ink,
                fontFamily = FontFamily.Monospace,
            )
        }
        Text(explanation(regime), color = BellColors.Muted, fontSize = 13.sp)
    }
}

@Composable
private fun InfoMeter(info: Double) {
    val scale = maxOf(info, 2.0) * 1.08
    Canvas(Modifier.fillMaxWidth().height(28.dp)) {
        val radius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
        drawRoundRect(BellColors.Line, cornerRadius = radius, size = Size(size.width, size.height))
        val fill = (info / scale).toFloat().coerceIn(0f, 1f)
        drawRoundRect(
            color = if (info > 1.0) BellColors.Alarm else BellColors.Ion,
            size = Size(size.width * fill, size.height),
            cornerRadius = radius,
        )
        val limit = (1.0 / scale).toFloat()
        drawLine(
            color = BellColors.Ink,
            start = Offset(size.width * limit, 0f),
            end = Offset(size.width * limit, size.height),
            strokeWidth = 2.dp.toPx(),
        )
    }
    Text(
        "Accessible information ${formatFixed(info, 2)} bits. The white mark is the one bit Alice sent.",
        color = BellColors.Muted,
        fontSize = 12.sp,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HandCard(hand: AccessHand) {
    Panel {
        Text(
            "Alice sends ${hand.message}. Bob asked for index ${hand.index} and guessed ${hand.guess}.",
            color = BellColors.Ink,
        )
        Text(
            text = if (hand.correct) "He named the right bit." else "He missed.",
            color = if (hand.correct) BellColors.Win else BellColors.Miss,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            hand.aliceBits.forEachIndexed { index, bit ->
                BitToken(index.toString(), bit, emphasize = index == hand.index)
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

private fun correlationOf(regime: Regime): Double = when (regime) {
    Regime.Classical -> 0.5
    Regime.Quantum -> ChshConstants.tsirelsonCorrelation
    Regime.PopescuRohrlich -> 1.0
}

private fun causalityVerdict(info: Double): String = if (info > 1.0) {
    "Breaks information causality. One sent bit unlocked ${formatFixed(info, 2)} bits."
} else {
    "Inside the one-bit budget. Bob can reach ${formatFixed(info, 2)} bits."
}

private fun explanation(regime: Regime): String = when (regime) {
    Regime.Classical ->
        "Local boxes are correlation 1/2. Nesting them still leaves Bob short of a full extra bit."

    Regime.Quantum ->
        "The qubit strategy is correlation 1/√2. Stack it as deep as you like: the sum stays under one bit."

    Regime.PopescuRohrlich ->
        "A PR box is correlation 1. Each extra layer doubles the string, and Bob still names his bit perfectly."
}

private fun deal(protocol: NestedRandomAccess, random: SeededRandom): AccessHand {
    val bits = IntArray(protocol.bitCount) { random.bit() }
    val index = random.nextInt(protocol.bitCount)
    return protocol.play(bits, index, random)
}

private fun runBatch(protocol: NestedRandomAccess, random: SeededRandom, trials: Int): Pair<Int, Int> {
    var correct = 0
    repeat(trials) {
        val bits = IntArray(protocol.bitCount) { random.bit() }
        val index = random.nextInt(protocol.bitCount)
        if (protocol.guess(bits, index, random) == bits[index]) {
            correct += 1
        }
    }
    return correct to trials
}

private const val BATCH = 400
