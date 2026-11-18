package dev.eyeoverthink.bellgame.engine

import kotlin.math.sqrt

/**
 * A complex amplitude used by the two-qubit simulator.
 *
 * The arithmetic is the ordinary field of complex numbers. States in this
 * project are lists of these amplitudes, and Born's rule reads probabilities
 * from [modulusSquared].
 */
data class Complex(val real: Double, val imaginary: Double) {
    /** This amplitude plus [other]. */
    operator fun plus(other: Complex): Complex = Complex(real + other.real, imaginary + other.imaginary)

    /** This amplitude minus [other]. */
    operator fun minus(other: Complex): Complex = Complex(real - other.real, imaginary - other.imaginary)

    /** Product of two complex numbers. */
    operator fun times(other: Complex): Complex = Complex(
        real * other.real - imaginary * other.imaginary,
        real * other.imaginary + imaginary * other.real,
    )

    /** Scale by a real number. */
    operator fun times(scalar: Double): Complex = Complex(real * scalar, imaginary * scalar)

    /** Complex conjugate. */
    fun conjugate(): Complex = Complex(real, -imaginary)

    /** Squared distance from the origin. This is the Born weight of an amplitude. */
    val modulusSquared: Double
        get() = real * real + imaginary * imaginary

    /** Distance from the origin. */
    val modulus: Double
        get() = sqrt(modulusSquared)

    companion object {
        /** The complex number zero. */
        val Zero = Complex(0.0, 0.0)

        /** The complex number one. */
        val One = Complex(1.0, 0.0)

        /** The imaginary unit. */
        val I = Complex(0.0, 1.0)

        /** A complex number that sits on the real axis. */
        fun real(value: Double): Complex = Complex(value, 0.0)
    }
}

/** Scale a complex number from the left by a real factor. */
operator fun Double.times(value: Complex): Complex = value * this
