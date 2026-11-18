package dev.eyeoverthink.bellgame.engine

/**
 * An isotropic non-signaling box for the CHSH predicate.
 *
 * Alice's output is a fair coin. Bob's output matches `alice xor (x and y)`
 * with probability `(1 + correlation) / 2`, and flips otherwise. The family
 * interpolates the three regimes:
 *
 * - correlation `1/2` wins 75% of rounds, the local ceiling, and CHSH value 2
 * - correlation `1/√2` wins `cos²(π/8)` of rounds, the qubit strategy
 * - correlation `1` wins every round, the Popescu-Rohrlich box, CHSH value 4
 *
 * Neither output, looked at alone, depends on the other player's input.
 */
object IsotropicBox {
    /**
     * Sample one use of the box.
     *
     * [correlation] is clamped only by the caller's contract; the probability
     * `(1 + correlation) / 2` must lie in the unit interval.
     */
    fun sample(x: Int, y: Int, correlation: Double, random: SeededRandom): Pair<Int, Int> {
        val alice = random.bit()
        val idealBob = alice xor (x and y)
        val faithful = random.nextDouble() < (1.0 + correlation) / 2.0
        val bob = if (faithful) idealBob else idealBob xor 1
        return alice to bob
    }

    /** Win probability of a box with this correlation. It does not depend on the inputs. */
    fun winProbability(correlation: Double): Double = (1.0 + correlation) / 2.0

    /** CHSH value `4E` of a box whose correlation strength is [correlation]. */
    fun chshValue(correlation: Double): Double = 4.0 * correlation
}

/**
 * The Popescu-Rohrlich box: a non-signaling resource that wins the CHSH game
 * on every input. It is the `correlation = 1` member of [IsotropicBox].
 */
object PrBox {
    /** Sample outputs that satisfy `a xor b == x and y` exactly. */
    fun sample(x: Int, y: Int, random: SeededRandom): Pair<Int, Int> =
        IsotropicBox.sample(x, y, correlation = 1.0, random = random)
}
