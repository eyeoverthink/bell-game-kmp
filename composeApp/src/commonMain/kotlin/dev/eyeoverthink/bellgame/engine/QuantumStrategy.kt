package dev.eyeoverthink.bellgame.engine

import kotlin.math.PI

/**
 * The four measurement angles, in radians, for one quantum strategy.
 *
 * Angle 0 points along Z. Positive angles rotate toward X. [tsirelson] is the
 * textbook optimum: Alice measures Z or X, and Bob measures `(Z ± X) / √2`.
 */
data class MeasurementAngles(
    val aliceWhenZero: Double,
    val aliceWhenOne: Double,
    val bobWhenZero: Double,
    val bobWhenOne: Double,
) {
    /** Alice's angle for input [x]. */
    fun alice(x: Int): Double = if (x == 0) aliceWhenZero else aliceWhenOne

    /** Bob's angle for input [y]. */
    fun bob(y: Int): Double = if (y == 0) bobWhenZero else bobWhenOne

    companion object {
        /**
         * Angles that saturate Tsirelson's bound on one Bell pair.
         *
         * Alice: `0` and `π/2`. Bob: `π/4` and `-π/4`.
         */
        val tsirelson = MeasurementAngles(
            aliceWhenZero = 0.0,
            aliceWhenOne = PI / 2.0,
            bobWhenZero = PI / 4.0,
            bobWhenOne = -PI / 4.0,
        )

        /** Everyone measures Z. The outputs always agree, so the win rate is 75%. */
        val aligned = MeasurementAngles(0.0, 0.0, 0.0, 0.0)
    }
}

/**
 * A quantum strategy: one shared pure state, and a measurement angle for each input.
 *
 * [analyticalWinRate] sums the Born probabilities. [sample] draws from the same
 * distribution, so a long run settles on the analytic curve.
 */
class QuantumStrategy(val angles: MeasurementAngles, val state: TwoQubitState = TwoQubitState.phiPlus) {
    /** Draw output bits for inputs [x] and [y]. */
    fun sample(x: Int, y: Int, random: SeededRandom): Pair<Int, Int> =
        state.sample(angles.alice(x), angles.bob(y), random)

    /** Probability that this input pair is a win, from the Born rule. */
    fun winProbability(x: Int, y: Int): Double {
        val target = x and y
        var total = 0.0
        for (alice in 0..1) {
            for (bob in 0..1) {
                if ((alice xor bob) == target) {
                    total += state.probability(alice, bob, angles.alice(x), angles.bob(y))
                }
            }
        }
        return total
    }

    /** Average win probability over the four equally likely inputs. */
    fun analyticalWinRate(): Double {
        var total = 0.0
        for (x in 0..1) {
            for (y in 0..1) {
                total += winProbability(x, y)
            }
        }
        return total / LocalTable.INPUT_PAIRS
    }

    /**
     * Correlator `⟨A B⟩` for this input pair.
     *
     * Outcome bit 0 contributes +1 and bit 1 contributes -1, so the correlator
     * is the probability the bits agree minus the probability they differ.
     */
    fun correlation(x: Int, y: Int): Double {
        var agree = 0.0
        var differ = 0.0
        for (alice in 0..1) {
            for (bob in 0..1) {
                val weight = state.probability(alice, bob, angles.alice(x), angles.bob(y))
                if (alice == bob) {
                    agree += weight
                } else {
                    differ += weight
                }
            }
        }
        return agree - differ
    }

    /**
     * The CHSH combination `E00 + E01 + E10 - E11`.
     *
     * Local strategies are at most 2. Quantum strategies on this screen are at
     * most `2√2`. The minus sign is on the input pair whose winning parity is 1.
     */
    fun chshValue(): Double = correlation(0, 0) + correlation(0, 1) + correlation(1, 0) - correlation(1, 1)

    companion object {
        /** The optimal Bell-pair strategy. */
        fun optimal(): QuantumStrategy = QuantumStrategy(MeasurementAngles.tsirelson)
    }
}
