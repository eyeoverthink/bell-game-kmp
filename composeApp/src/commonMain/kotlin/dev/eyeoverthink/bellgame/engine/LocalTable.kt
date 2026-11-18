package dev.eyeoverthink.bellgame.engine

/**
 * A deterministic local strategy for the CHSH game.
 *
 * Alice's output is a function of her bit alone, and Bob's output is a function
 * of his bit alone. There are sixteen such tables. Shared randomness picks a
 * table at random and cannot beat the best single table, because a mixture's
 * win rate is the weighted average of the tables' win rates.
 */
data class LocalTable(val aliceWhenZero: Int, val aliceWhenOne: Int, val bobWhenZero: Int, val bobWhenOne: Int) {
    init {
        require(aliceWhenZero in BIT_RANGE && aliceWhenOne in BIT_RANGE) {
            "Alice's outputs must be bits."
        }
        require(bobWhenZero in BIT_RANGE && bobWhenOne in BIT_RANGE) {
            "Bob's outputs must be bits."
        }
    }

    /** Alice's committed output on input [x]. */
    fun alice(x: Int): Int = if (x == 0) aliceWhenZero else aliceWhenOne

    /** Bob's committed output on input [y]. */
    fun bob(y: Int): Int = if (y == 0) bobWhenZero else bobWhenOne

    /**
     * Win rate over the four input pairs, each equally likely.
     *
     * The value is exact: it is `wins / 4`, and 0.75 is the highest any table reaches.
     */
    fun exactWinRate(): Double {
        var wins = 0
        for (x in 0..1) {
            for (y in 0..1) {
                if (Chsh.won(alice(x), bob(y), x, y)) {
                    wins += 1
                }
            }
        }
        return wins / INPUT_PAIRS
    }

    /** Whether this table wins the deal `(x, y)`. */
    fun winsInput(x: Int, y: Int): Boolean = Chsh.won(alice(x), bob(y), x, y)

    companion object {
        private val BIT_RANGE = 0..1

        /** Number of equally likely input pairs. */
        const val INPUT_PAIRS = 4.0

        /** Both players always output 0. This wins every deal except `(1, 1)`. */
        val alwaysZero = LocalTable(0, 0, 0, 0)

        /** Every deterministic local strategy, in a stable order. */
        fun all(): List<LocalTable> {
            val bits = intArrayOf(0, 1)
            val tables = ArrayList<LocalTable>(TABLE_COUNT)
            for (aliceZero in bits) {
                for (aliceOne in bits) {
                    for (bobZero in bits) {
                        for (bobOne in bits) {
                            tables += LocalTable(aliceZero, aliceOne, bobZero, bobOne)
                        }
                    }
                }
            }
            return tables
        }

        /** Highest exact win rate among [all]. */
        fun bestWinRate(): Double = all().maxOf { it.exactWinRate() }

        private const val TABLE_COUNT = 16
    }
}
