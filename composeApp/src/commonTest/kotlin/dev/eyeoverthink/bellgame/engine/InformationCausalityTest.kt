package dev.eyeoverthink.bellgame.engine

import kotlin.math.abs
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InformationCausalityTest {
    @Test
    fun binaryEntropyHasTheFairAndCertainLimits() {
        assertEquals(0.0, binaryEntropy(0.0), 0.0)
        assertEquals(0.0, binaryEntropy(1.0), 0.0)
        assertEquals(1.0, binaryEntropy(0.5), 1e-12)
    }

    @Test
    fun prBoxViolatesTheOneBitBudget() {
        assertTrue(InformationCausality.violates(correlation = 1.0, layers = 1))
        assertEquals(2.0, InformationCausality.accessibleBits(1.0, 1), 1e-12)
        assertEquals(8.0, InformationCausality.accessibleBits(1.0, 3), 1e-12)
        assertTrue(InformationCausality.violatesSomewhere(1.0, maxLayers = 4))
    }

    @Test
    fun quantumCorrelationStaysInsideTheBudget() {
        val correlation = ChshConstants.tsirelsonCorrelation
        for (layers in 1..16) {
            val info = InformationCausality.accessibleBits(correlation, layers)
            assertTrue(info <= 1.0, "layers=$layers accessible=$info")
            assertTrue(!InformationCausality.violates(correlation, layers))
        }
        assertTrue(!InformationCausality.violatesSomewhere(correlation, maxLayers = 16))
    }

    @Test
    fun classicalCorrelationStaysInsideTheBudget() {
        for (layers in 1..12) {
            val info = InformationCausality.accessibleBits(0.5, layers)
            assertTrue(info < 1.0, "layers=$layers accessible=$info")
        }
        val quantum = InformationCausality.accessibleBits(ChshConstants.tsirelsonCorrelation, 1)
        val classical = InformationCausality.accessibleBits(0.5, 1)
        val pr = InformationCausality.accessibleBits(1.0, 1)
        assertTrue(classical < quantum)
        assertTrue(quantum < 1.0)
        assertTrue(pr > 1.0)
    }

    @Test
    fun nestedPrProtocolIsPerfect() {
        for (layers in 1..2) {
            val protocol = NestedRandomAccess(layers = layers, correlation = 1.0)
            val random = SeededRandom(11)
            val masks = 1 shl protocol.bitCount
            for (mask in 0 until masks) {
                val bits = IntArray(protocol.bitCount) { bit -> (mask shr bit) and 1 }
                for (index in 0 until protocol.bitCount) {
                    repeat(3) {
                        assertEquals(bits[index], protocol.guess(bits, index, random))
                    }
                }
            }
        }
    }

    @Test
    fun noisyNestingMatchesTheClosedForm() {
        val correlation = ChshConstants.tsirelsonCorrelation
        val layers = 2
        val protocol = NestedRandomAccess(layers, correlation)
        val random = SeededRandom(99)
        val trials = 6_000
        var correct = 0
        repeat(trials) {
            val bits = IntArray(protocol.bitCount) { random.bit() }
            val index = random.nextInt(protocol.bitCount)
            if (protocol.guess(bits, index, random) == bits[index]) {
                correct += 1
            }
        }
        val expected = (1.0 + correlation.pow(layers)) / 2.0
        val sampled = correct.toDouble() / trials
        assertEquals(expected, InformationCausality.successProbability(correlation, layers), 1e-12)
        assertTrue(
            abs(sampled - expected) < 0.03,
            "Sampled success $sampled versus closed form $expected",
        )
    }
}
