package dev.eyeoverthink.bellgame.engine

/**
 * A running score for a stream of CHSH deals.
 *
 * The four input pairs are counted separately so the table can show which
 * deal is dragging the average down. [curve] stores a downsampled history of
 * the win rate for the convergence chart.
 */
data class Ledger(
    val rounds: Int = 0,
    val wins: Int = 0,
    val roundsByInput: List<Int> = List(INPUTS) { 0 },
    val winsByInput: List<Int> = List(INPUTS) { 0 },
    val curve: List<Double> = emptyList(),
) {
    /** Wins divided by deals, or 0 before the first deal. */
    val winRate: Double
        get() = if (rounds == 0) 0.0 else wins.toDouble() / rounds

    /** Empirical win rate on the single input pair `(x, y)`. */
    fun rateFor(x: Int, y: Int): Double {
        val slot = slotOf(x, y)
        val dealt = roundsByInput[slot]
        if (dealt == 0) {
            return 0.0
        }
        return winsByInput[slot].toDouble() / dealt
    }

    /** How many times the input pair `(x, y)` has been dealt. */
    fun roundsFor(x: Int, y: Int): Int = roundsByInput[slotOf(x, y)]

    /** Record one deal. */
    fun record(x: Int, y: Int, won: Boolean): Ledger {
        val slot = slotOf(x, y)
        val nextRoundsBy = roundsByInput.toMutableList()
        val nextWinsBy = winsByInput.toMutableList()
        nextRoundsBy[slot] = nextRoundsBy[slot] + 1
        if (won) {
            nextWinsBy[slot] = nextWinsBy[slot] + 1
        }
        val nextRounds = rounds + 1
        val nextWins = wins + if (won) 1 else 0
        val rate = nextWins.toDouble() / nextRounds
        val nextCurve = if (nextRounds == 1 || nextRounds % CURVE_STRIDE == 0) {
            downsample(curve + rate)
        } else {
            curve
        }
        return copy(
            rounds = nextRounds,
            wins = nextWins,
            roundsByInput = nextRoundsBy,
            winsByInput = nextWinsBy,
            curve = nextCurve,
        )
    }

    private fun downsample(points: List<Double>): List<Double> {
        if (points.size <= CURVE_CAP) {
            return points
        }
        return points.filterIndexed { index, _ -> index % 2 == 0 }
    }

    companion object {
        private const val INPUTS = 4
        private const val CURVE_STRIDE = 4
        private const val CURVE_CAP = 240

        /** Index of input pair `(x, y)` in the four-cell table. */
        fun slotOf(x: Int, y: Int): Int = x * 2 + y
    }
}
