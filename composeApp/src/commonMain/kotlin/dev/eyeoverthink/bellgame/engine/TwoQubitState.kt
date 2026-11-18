package dev.eyeoverthink.bellgame.engine

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The eigenvector of a spin measurement in the XZ plane.
 *
 * [up] is the amplitude of computational `|0>` and [down] is the amplitude of
 * `|1>`. Outcome 0 is the +1 eigenvalue of `cos(angle) Z + sin(angle) X`.
 * Outcome 1 is the -1 eigenvalue. Mapping +1 to bit 0 makes "same bit" the
 * same event as "same spin".
 */
data class Spinor(val up: Complex, val down: Complex)

/**
 * Spin measurement direction [angle] radians from Z toward X, for one outcome bit.
 */
fun measurementSpinor(angle: Double, outcome: Int): Spinor {
    val half = angle / 2.0
    val cosine = cos(half)
    val sine = sin(half)
    return if (outcome == 0) {
        Spinor(Complex.real(cosine), Complex.real(sine))
    } else {
        Spinor(Complex.real(-sine), Complex.real(cosine))
    }
}

/**
 * A pure state of two qubits, Alice then Bob, written in the computational basis.
 *
 * Index `2 * alice + bob` holds the amplitude of `|alice, bob>`. The simulator
 * applies local spin measurements by projecting onto the product of two
 * [Spinor]s and reading the Born weight. No network and no native library is
 * involved: four complex numbers are the whole state.
 */
data class TwoQubitState(val amplitudes: List<Complex>) {
    init {
        require(amplitudes.size == BASIS_SIZE) {
            "A two-qubit state needs $BASIS_SIZE amplitudes."
        }
    }

    /** Amplitude of computational basis vector `|alice, bob>`. */
    fun amplitude(alice: Int, bob: Int): Complex = amplitudes[alice * 2 + bob]

    /**
     * Born probability that Alice obtains [alice] and Bob obtains [bob]
     * when they measure at [aliceAngle] and [bobAngle].
     */
    fun probability(alice: Int, bob: Int, aliceAngle: Double, bobAngle: Double): Double =
        jointAmplitude(alice, bob, aliceAngle, bobAngle).modulusSquared

    /**
     * Draw one joint outcome from the Born distribution of the two measurements.
     */
    fun sample(aliceAngle: Double, bobAngle: Double, random: SeededRandom): Pair<Int, Int> {
        val roll = random.nextDouble()
        var cumulative = 0.0
        for (alice in 0..1) {
            for (bob in 0..1) {
                cumulative += probability(alice, bob, aliceAngle, bobAngle)
                if (roll <= cumulative) {
                    return alice to bob
                }
            }
        }
        return 1 to 1
    }

    /**
     * Projection of this state onto Alice's outcome and Bob's outcome.
     *
     * The bra is the conjugate of the product spinor, so a relative phase on
     * either qubit is visible to the amplitude and invisible to a global phase.
     */
    fun jointAmplitude(alice: Int, bob: Int, aliceAngle: Double, bobAngle: Double): Complex {
        val aliceSpinor = measurementSpinor(aliceAngle, alice)
        val bobSpinor = measurementSpinor(bobAngle, bob)
        val aliceParts = listOf(aliceSpinor.up, aliceSpinor.down)
        val bobParts = listOf(bobSpinor.up, bobSpinor.down)
        var sum = Complex.Zero
        for (aliceBasis in 0..1) {
            for (bobBasis in 0..1) {
                val bra = aliceParts[aliceBasis].conjugate() * bobParts[bobBasis].conjugate()
                sum += bra * amplitude(aliceBasis, bobBasis)
            }
        }
        return sum
    }

    /** Sum of the squared moduli. A physical state returns 1. */
    fun totalWeight(): Double = amplitudes.sumOf { it.modulusSquared }

    companion object {
        /** Dimension of the two-qubit computational basis. */
        const val BASIS_SIZE = 4

        /**
         * The Bell state `|Φ+> = (|00> + |11>) / √2`.
         *
         * Alice and Bob each hold one qubit. Measuring both along the same
         * XZ direction makes their outcome bits agree.
         */
        val phiPlus: TwoQubitState = run {
            val scale = 1.0 / sqrt(2.0)
            TwoQubitState(
                listOf(
                    Complex.real(scale),
                    Complex.Zero,
                    Complex.Zero,
                    Complex.real(scale),
                ),
            )
        }
    }
}
