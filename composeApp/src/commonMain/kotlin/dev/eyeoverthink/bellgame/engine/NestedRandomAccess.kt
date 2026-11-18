package dev.eyeoverthink.bellgame.engine

/**
 * One played hand of the nested random-access protocol.
 *
 * [message] is the single classical bit Alice sends after she has used her
 * boxes. [guess] is Bob's reconstruction of `aliceBits[index]`.
 */
data class AccessHand(val aliceBits: List<Int>, val index: Int, val message: Int, val guess: Int) {
    /** Whether Bob named the bit he asked for. */
    val correct: Boolean
        get() = guess == aliceBits[index]
}

/**
 * Van Dam's nested use of CHSH boxes, written as a random-access game.
 *
 * Alice's string has length `2^layers`. Each level pairs adjacent bits. Alice
 * feeds the parity of a pair into a box and replaces the pair by
 * `left xor (her box output)`. Bob, who wants one index, queries only the pair
 * that contains that index and folds the box output into a running parity.
 * After [layers] levels Alice has a single bit left. She sends it. Bob xors
 * that bit with everything his boxes returned.
 *
 * When every box is a PR box, the guess equals the requested bit. When each
 * box is only right with probability `(1 + E) / 2`, an error anywhere on Bob's
 * path flips the guess, and the closed form in [InformationCausality] counts
 * the damage.
 */
class NestedRandomAccess(val layers: Int, val correlation: Double) {
    init {
        require(layers in 1..MAX_LAYERS) {
            "Nesting depth must be from 1 to $MAX_LAYERS."
        }
        require(correlation in CORRELATION_RANGE) {
            "Correlation must lie between -1 and 1."
        }
    }

    /** How many bits Alice holds. */
    val bitCount: Int = 1 shl layers

    /**
     * Run one hand. [bits] has length [bitCount] and [index] selects the bit Bob wants.
     */
    fun guess(bits: IntArray, index: Int, random: SeededRandom): Int {
        require(bits.size == bitCount)
        require(index in 0 until bitCount)
        return play(bits, index, random).guess
    }

    /**
     * Run one hand and keep the message as well as the guess, for the exhibit.
     */
    fun play(bits: IntArray, index: Int, random: SeededRandom): AccessHand {
        require(bits.size == bitCount)
        require(index in 0 until bitCount)
        var current = bits.copyOf()
        var bobParity = 0
        var cursor = index
        repeat(layers) {
            val pairs = current.size / 2
            val next = IntArray(pairs)
            val bobInput = cursor and 1
            val chosen = cursor ushr 1
            for (pair in 0 until pairs) {
                val left = current[pair * 2]
                val right = current[pair * 2 + 1]
                val aliceInput = left xor right
                val aliceOut = if (pair == chosen) {
                    val (boxAlice, boxBob) = IsotropicBox.sample(
                        aliceInput,
                        bobInput,
                        correlation,
                        random,
                    )
                    bobParity = bobParity xor boxBob
                    boxAlice
                } else {
                    random.bit()
                }
                next[pair] = left xor aliceOut
            }
            current = next
            cursor = chosen
        }
        val message = current[0]
        return AccessHand(
            aliceBits = bits.toList(),
            index = index,
            message = message,
            guess = message xor bobParity,
        )
    }

    companion object {
        /** Deep enough to show 256 bits, small enough to deal by hand in the UI. */
        const val MAX_LAYERS = 8

        private val CORRELATION_RANGE = -1.0..1.0
    }
}
