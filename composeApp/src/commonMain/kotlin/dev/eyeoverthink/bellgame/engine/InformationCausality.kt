package dev.eyeoverthink.bellgame.engine

import kotlin.math.log2
import kotlin.math.pow

/**
 * Binary entropy of a coin with heads-probability [probability], in bits.
 *
 * The endpoints are defined by continuity: a coin that always lands the same
 * way carries no surprise, so the entropy is 0. A fair coin has entropy 1.
 */
fun binaryEntropy(probability: Double): Double {
    val clamped = probability.coerceIn(0.0, 1.0)
    if (clamped == 0.0 || clamped == 1.0) {
        return 0.0
    }
    val other = 1.0 - clamped
    return -(clamped * log2(clamped) + other * log2(other))
}

/**
 * The van Dam / Pawłowski information-causality ledger for isotropic boxes.
 *
 * Alice holds `2^layers` independent bits and may send one classical bit.
 * Bob may ask for any one of them. With a box of correlation `E` at each
 * level of the nesting, his guess is right with probability `(1 + E^layers) / 2`.
 * Each of Alice's bits then contributes `1 - h(p)` bits of accessible
 * information, and the sum over the string is
 *
 * `2^layers * (1 - h((1 + E^layers) / 2))`.
 *
 * Information causality says that sum cannot exceed the one bit Alice sent.
 * The PR box (`E = 1`) reports `2^layers` bits. The quantum correlation
 * `E = 1/√2` stays at or under one bit. The local correlation `E = 1/2` does too.
 */
object InformationCausality {
    /** Largest nesting depth the closed form is asked to evaluate. */
    const val MAX_LAYERS = 20

    /** Success probability of the nested protocol. */
    fun successProbability(correlation: Double, layers: Int): Double {
        require(layers in 1..MAX_LAYERS)
        return (1.0 + correlation.pow(layers)) / 2.0
    }

    /**
     * Sum of mutual informations Bob can reach about Alice's bits, in bits.
     *
     * This is the quantity information causality bounds by the size of Alice's
     * classical message. Here that message is one bit, so a result above 1 is
     * a violation.
     */
    fun accessibleBits(correlation: Double, layers: Int): Double {
        val success = successProbability(correlation, layers)
        val perBit = 1.0 - binaryEntropy(success)
        return (1 shl layers).toDouble() * perBit
    }

    /** Whether [accessibleBits] is strictly above the one bit Alice sent. */
    fun violates(correlation: Double, layers: Int): Boolean =
        accessibleBits(correlation, layers) > ONE_BIT + NUMERIC_SLACK

    /**
     * Whether any nesting depth from 1 through [maxLayers] breaks the one-bit budget.
     */
    fun violatesSomewhere(correlation: Double, maxLayers: Int): Boolean {
        require(maxLayers in 1..MAX_LAYERS)
        for (layers in 1..maxLayers) {
            if (violates(correlation, layers)) {
                return true
            }
        }
        return false
    }

    private const val ONE_BIT = 1.0
    private const val NUMERIC_SLACK = 1e-9
}
