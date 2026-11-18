package dev.eyeoverthink.bellgame.engine

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrBoxTest {
    @Test
    fun everySampleWins() {
        val random = SeededRandom(5)
        for (x in 0..1) {
            for (y in 0..1) {
                repeat(300) {
                    val (a, b) = PrBox.sample(x, y, random)
                    assertTrue(Chsh.won(a, b, x, y))
                }
            }
        }
    }

    @Test
    fun winRateAndChshValueAreExact() {
        assertEquals(ChshConstants.PR_WIN_RATE, IsotropicBox.winProbability(1.0), 0.0)
        assertEquals(ChshConstants.PR_CHSH, IsotropicBox.chshValue(1.0), 0.0)
        assertEquals(
            ChshConstants.tsirelsonWinRate,
            IsotropicBox.winProbability(ChshConstants.tsirelsonCorrelation),
            1e-12,
        )
        assertEquals(
            ChshConstants.tsirelsonChsh,
            IsotropicBox.chshValue(ChshConstants.tsirelsonCorrelation),
            1e-12,
        )
        assertEquals(ChshConstants.CLASSICAL_WIN_RATE, IsotropicBox.winProbability(0.5), 0.0)
        assertEquals(ChshConstants.CLASSICAL_CHSH, IsotropicBox.chshValue(0.5), 0.0)
    }

    @Test
    fun marginalsDoNotSignal() {
        val random = SeededRandom(19)
        val samples = 4_000
        for (x in 0..1) {
            val aliceByBobInput = DoubleArray(2)
            for (y in 0..1) {
                var aliceOnes = 0
                repeat(samples) {
                    aliceOnes += PrBox.sample(x, y, random).first
                }
                aliceByBobInput[y] = aliceOnes.toDouble() / samples
            }
            assertTrue(abs(aliceByBobInput[0] - aliceByBobInput[1]) < 0.05)
            assertTrue(abs(aliceByBobInput[0] - 0.5) < 0.05)
        }
        val bobRandom = SeededRandom(23)
        for (y in 0..1) {
            val bobByAlice = DoubleArray(2)
            for (x in 0..1) {
                var bobOnes = 0
                repeat(samples) {
                    bobOnes += PrBox.sample(x, y, bobRandom).second
                }
                bobByAlice[x] = bobOnes.toDouble() / samples
            }
            assertTrue(abs(bobByAlice[0] - bobByAlice[1]) < 0.05)
            assertTrue(abs(bobByAlice[0] - 0.5) < 0.05)
        }
    }
}
