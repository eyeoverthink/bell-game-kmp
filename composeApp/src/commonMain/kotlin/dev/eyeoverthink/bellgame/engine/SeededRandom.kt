package dev.eyeoverthink.bellgame.engine

import kotlin.random.Random

/**
 * A seedable source of bits and uniform doubles.
 *
 * The same seed produces the same stream on every target, so a round of the
 * game can be replayed. One instance is not safe to share across threads.
 */
class SeededRandom(seed: Long) {
    private val source = Random(seed)

    /** A fair bit, 0 or 1. */
    fun bit(): Int = if (source.nextBoolean()) 1 else 0

    /** A uniform sample from the half-open unit interval. */
    fun nextDouble(): Double = source.nextDouble()

    /** A uniform integer in `0` until [until]. */
    fun nextInt(until: Int): Int = source.nextInt(until)

    /** A new generator whose seed is the next long from this stream. */
    fun fork(): SeededRandom = SeededRandom(source.nextLong())
}
