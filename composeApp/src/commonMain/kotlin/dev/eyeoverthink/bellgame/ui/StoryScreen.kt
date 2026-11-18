package dev.eyeoverthink.bellgame.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A short tour of the three ceilings, in ordinary language.
 */
@Composable
fun StoryScreen(modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Why the score stops", style = MaterialTheme.typography.headlineMedium, color = BellColors.Ink)
        StoryBlock(
            kicker = "The game",
            body = "Alice and Bob are apart. A referee hands Alice a bit x and Bob a bit y, " +
                "each fair and independent. Alice answers with a bit a. Bob answers with a bit b. " +
                "They win the round when a XOR b equals x AND y. They may agree on a plan beforehand. " +
                "During the round they may not talk.",
        )
        StoryBlock(
            kicker = "75% is a wall",
            body = "If each player's answer is a function of the bit they were handed, there are " +
                "sixteen possible plans. The best of them wins three of the four possible deals " +
                "and loses the fourth. Mixing plans with a shared coin flip only averages those " +
                "sixteen scores, so it cannot climb past 75%. On the table, flip the switches: " +
                "the locked-in rate never reads 100%.",
        )
        StoryBlock(
            kicker = "85.36% is as far as a qubit pair goes",
            body = "Give them one entangled pair, the Bell state that is an equal mix of both " +
                "bits zero and both bits one. Alice measures along Z or X, depending on x. " +
                "Bob measures along the two diagonals between Z and X, depending on y. " +
                "The chance they win a round is cos²(π/8), about 85.36%. The same angles " +
                "produce the CHSH number 2√2, Tsirelson's bound. The bench lets you leave " +
                "those angles and watch the win rate fall back toward 75%, or sit on the bound " +
                "while a Monte Carlo run settles onto the curve.",
        )
        StoryBlock(
            kicker = "100% spends more than was sent",
            body = "A Popescu-Rohrlich box wins every round and still does not let Alice's " +
                "input change Bob's local statistics, or the other way around. That sounds free. " +
                "It is not. Stack copies of the box into van Dam's nesting: Alice holds 2, 4, 8, " +
                "16, or 32 bits and sends a single classical bit, and Bob can name any bit he " +
                "points at. The information he gains, summed over the string, is the whole string. " +
                "Information causality says that sum cannot exceed the one bit she sent. " +
                "The quantum correlation stays under the budget. The perfect box does not. " +
                "Nature, as far as we can play it, stops at the qubit line.",
        )
    }
}

@Composable
private fun StoryBlock(kicker: String, body: String) {
    Panel {
        Text(kicker, color = BellColors.Ion, fontSize = 13.sp)
        Text(body, color = BellColors.Ink, style = MaterialTheme.typography.bodyLarge)
    }
}
