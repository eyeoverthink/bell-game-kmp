package dev.eyeoverthink.bellgame.engine

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuantumStrategyTest {
    @Test
    fun bellPairIsNormalized() {
        assertEquals(1.0, TwoQubitState.phiPlus.totalWeight(), 1e-12)
    }

    @Test
    fun bornProbabilitiesSumToOne() {
        val state = TwoQubitState.phiPlus
        val angles = listOf(0.0, 0.7, -0.4, PI / 2)
        for (alice in angles) {
            for (bob in angles) {
                var total = 0.0
                for (outcomeA in 0..1) {
                    for (outcomeB in 0..1) {
                        total += state.probability(outcomeA, outcomeB, alice, bob)
                    }
                }
                assertEquals(1.0, total, 1e-12)
            }
        }
    }

    @Test
    fun agreementFollowsTheHalfAngleCosine() {
        val state = TwoQubitState.phiPlus
        val angles = listOf(0.0, 0.35, -0.8, PI / 2, PI / 4)
        for (alice in angles) {
            for (bob in angles) {
                var agree = 0.0
                for (outcomeA in 0..1) {
                    for (outcomeB in 0..1) {
                        if (outcomeA == outcomeB) {
                            agree += state.probability(outcomeA, outcomeB, alice, bob)
                        }
                    }
                }
                val expected = cos((alice - bob) / 2.0).pow(2)
                assertEquals(expected, agree, 1e-9)
            }
        }
    }

    @Test
    fun localMarginalIsOneHalfForAnyRemoteAngle() {
        val state = TwoQubitState.phiPlus
        val remote = listOf(0.0, 0.4, -1.1, PI / 2)
        for (bob in remote) {
            var heads = 0.0
            for (outcomeB in 0..1) {
                heads += state.probability(0, outcomeB, 0.3, bob)
            }
            assertEquals(0.5, heads, 1e-12)
        }
    }

    @Test
    fun relativePhaseChangesInterference() {
        val phased = TwoQubitState(
            listOf(
                TwoQubitState.phiPlus.amplitude(0, 0),
                TwoQubitState.phiPlus.amplitude(0, 1),
                TwoQubitState.phiPlus.amplitude(1, 0),
                TwoQubitState.phiPlus.amplitude(1, 1) * Complex.I,
            ),
        )
        assertEquals(1.0, phased.totalWeight(), 1e-12)
        val base = TwoQubitState.phiPlus.probability(0, 0, PI / 2, PI / 2)
        val moved = phased.probability(0, 0, PI / 2, PI / 2)
        assertTrue(abs(base - moved) > 0.1)
    }

    @Test
    fun optimalAnglesHitTheTsirelsonWinRate() {
        val strategy = QuantumStrategy.optimal()
        assertEquals(ChshConstants.tsirelsonWinRate, strategy.analyticalWinRate(), 1e-12)
        assertEquals(0.8536, strategy.analyticalWinRate(), 1e-4)
        assertEquals(ChshConstants.tsirelsonChsh, strategy.chshValue(), 1e-12)
        for (x in 0..1) {
            for (y in 0..1) {
                assertEquals(ChshConstants.tsirelsonWinRate, strategy.winProbability(x, y), 1e-12)
            }
        }
    }

    @Test
    fun alignedMeasurementsStayAtTheLocalCeiling() {
        val strategy = QuantumStrategy(MeasurementAngles.aligned)
        assertEquals(ChshConstants.CLASSICAL_WIN_RATE, strategy.analyticalWinRate(), 1e-12)
        assertEquals(ChshConstants.CLASSICAL_CHSH, strategy.chshValue(), 1e-12)
    }

    @Test
    fun monteCarloAgreesWithTheAnalyticRate() {
        val strategy = QuantumStrategy.optimal()
        val random = SeededRandom(20261118L)
        val trials = 20_000
        var wins = 0
        repeat(trials) {
            val (x, y) = MatchEngine.inputs(random)
            val (a, b) = strategy.sample(x, y, random)
            if (Chsh.won(a, b, x, y)) {
                wins += 1
            }
        }
        val rate = wins.toDouble() / trials
        assertTrue(
            abs(rate - ChshConstants.tsirelsonWinRate) < 0.02,
            "Sampled win rate $rate was outside tolerance of ${ChshConstants.tsirelsonWinRate}",
        )
    }
}
