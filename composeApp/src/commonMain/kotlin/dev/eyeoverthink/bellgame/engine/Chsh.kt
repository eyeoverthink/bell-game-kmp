package dev.eyeoverthink.bellgame.engine

import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Numbers that separate the three regimes of the CHSH game.
 *
 * Classical shared randomness cannot push the win rate above [CLASSICAL_WIN_RATE].
 * Quantum mechanics, with one shared Bell pair, tops out at [tsirelsonWinRate],
 * which is `cos²(π/8)`. A Popescu-Rohrlich box wins every round and is not a
 * quantum strategy.
 */
object ChshConstants {
    /** Win rate of the best local deterministic tables, and of any mixture of them. */
    const val CLASSICAL_WIN_RATE = 0.75

    /** CHSH correlator achieved by those same local tables. */
    const val CLASSICAL_CHSH = 2.0

    /** Win rate of a Popescu-Rohrlich box. */
    const val PR_WIN_RATE = 1.0

    /** CHSH correlator of a Popescu-Rohrlich box. */
    const val PR_CHSH = 4.0

    /** Correlation strength `1/√2` realized by the optimal qubit angles. */
    val tsirelsonCorrelation: Double = 1.0 / sqrt(2.0)

    /** Tsirelson's bound on the CHSH correlator, `2√2`. */
    val tsirelsonChsh: Double = 2.0 * sqrt(2.0)

    /** Optimal quantum win rate, `cos²(π/8)`. */
    val tsirelsonWinRate: Double = cos(kotlin.math.PI / 8.0).pow(2)
}

/**
 * Which resource Alice and Bob are allowed to share.
 */
enum class Regime(val title: String, val ceiling: String) {
    /** Outputs are functions of the local input bit only. */
    Classical("Classical", "75%"),

    /** One shared Bell pair, measured in the XZ plane. */
    Quantum("Quantum", "85.36%"),

    /** A non-signaling box that wins every input. It is not physical. */
    PopescuRohrlich("PR box", "100%"),
}

/**
 * One deal of the CHSH game.
 *
 * [x] is Alice's input, [y] is Bob's input, [a] is Alice's output, and [b] is
 * Bob's output. They win when `a xor b` equals `x and y`.
 */
data class Round(val x: Int, val y: Int, val a: Int, val b: Int) {
    /** Parity the rules demand of the two outputs. */
    val requiredParity: Int
        get() = x and y

    /** Parity the players actually produced. */
    val actualParity: Int
        get() = a xor b

    /** Whether this deal is a win. */
    val won: Boolean
        get() = Chsh.won(a, b, x, y)
}

/**
 * The rules of the CHSH game, and the small amount of text the table needs
 * in order to explain a single deal.
 */
object Chsh {
    /**
     * Players win when the parity of their outputs equals the AND of their inputs.
     */
    fun won(a: Int, b: Int, x: Int, y: Int): Boolean = (a xor b) == (x and y)

    /**
     * A one-sentence account of why [round] was a win or a miss.
     */
    fun explain(round: Round): String {
        val need = if (round.requiredParity == 0) "agree" else "disagree"
        val did = if (round.actualParity == 0) "agreed" else "disagreed"
        val mark = if (round.won) "Win." else "Miss."
        return "x AND y = ${round.requiredParity}, so the outputs needed to $need. They $did. $mark"
    }
}

/**
 * Deals inputs and asks the selected regime for outputs.
 *
 * The referee sees both inputs. The strategies do not: a classical table is a
 * pair of local functions, the quantum strategy measures one shared state, and
 * the PR box is sampled as a single non-signaling resource.
 */
object MatchEngine {
    /** Two independent fair input bits. */
    fun inputs(random: SeededRandom): Pair<Int, Int> = random.bit() to random.bit()

    /** Play one round under [regime]. */
    fun round(
        regime: Regime,
        table: LocalTable,
        angles: MeasurementAngles,
        x: Int,
        y: Int,
        random: SeededRandom,
    ): Round {
        val outputs = when (regime) {
            Regime.Classical -> table.alice(x) to table.bob(y)
            Regime.Quantum -> QuantumStrategy(angles).sample(x, y, random)
            Regime.PopescuRohrlich -> PrBox.sample(x, y, random)
        }
        return Round(x, y, outputs.first, outputs.second)
    }
}
