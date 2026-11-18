package dev.eyeoverthink.bellgame.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.eyeoverthink.bellgame.engine.Chsh
import dev.eyeoverthink.bellgame.engine.Ledger
import dev.eyeoverthink.bellgame.engine.LocalTable
import dev.eyeoverthink.bellgame.engine.MatchEngine
import dev.eyeoverthink.bellgame.engine.MeasurementAngles
import dev.eyeoverthink.bellgame.engine.QuantumStrategy
import dev.eyeoverthink.bellgame.engine.Regime
import dev.eyeoverthink.bellgame.engine.Round
import dev.eyeoverthink.bellgame.engine.SeededRandom

/**
 * How fast the table deals itself.
 */
enum class Pace(val delayMillis: Long, val label: String) {
    Slow(640, "Slow"),
    Live(140, "Live"),
    Flood(16, "Flood"),
}

/**
 * Shared table state. The engine underneath is pure; this object only holds
 * the knobs the screens share.
 */
class GameSession(seed: Long = DEFAULT_SEED) {
    var seed by mutableStateOf(seed)
        private set

    private var random = SeededRandom(seed)

    var regime by mutableStateOf(Regime.Classical)
        private set

    var table by mutableStateOf(LocalTable.alwaysZero)
        private set

    var angles by mutableStateOf(MeasurementAngles.tsirelson)
        private set

    var running by mutableStateOf(false)

    var pace by mutableStateOf(Pace.Live)

    var ledger by mutableStateOf(Ledger())
        private set

    var labLedger by mutableStateOf(Ledger())
        private set

    var lastRound by mutableStateOf<Round?>(null)
        private set

    var notice by mutableStateOf("Pick a strategy, then deal a round.")
        private set

    /** Deal one round with the current regime and record it. */
    fun playOne() {
        val (x, y) = MatchEngine.inputs(random)
        val round = MatchEngine.round(regime, table, angles, x, y, random)
        lastRound = round
        ledger = ledger.record(x, y, round.won)
        notice = Chsh.explain(round)
    }

    /** Switch resource. The score starts over so two regimes are never mixed. */
    fun selectRegime(next: Regime) {
        if (regime == next) {
            return
        }
        regime = next
        running = false
        clearTable("Score cleared. ${next.title} is a different resource.")
    }

    /** Replace the local truth table. */
    fun updateTable(next: LocalTable) {
        table = next
        if (regime == Regime.Classical) {
            running = false
            clearTable("Score cleared. The local table changed.")
        }
    }

    /** Replace the four measurement angles and drop the bench sample. */
    fun updateAngles(next: MeasurementAngles) {
        angles = next
        labLedger = Ledger()
        if (regime == Regime.Quantum) {
            running = false
            clearTable("Score cleared. The angles changed.")
        }
    }

    /** Sample [count] quantum rounds at the current angles into the bench ledger. */
    fun recordLabRounds(count: Int) {
        val strategy = QuantumStrategy(angles)
        var next = labLedger
        repeat(count) {
            val (x, y) = MatchEngine.inputs(random)
            val (alice, bob) = strategy.sample(x, y, random)
            next = next.record(x, y, Chsh.won(alice, bob, x, y))
        }
        labLedger = next
    }

    /** Drop the bench sample without touching the table score. */
    fun clearLab() {
        labLedger = Ledger()
    }

    /** Stop the table and zero its score. */
    fun resetTable() {
        running = false
        clearTable("Score cleared.")
    }

    /** Advance the seed by one so the next deals are a new, still replayable, stream. */
    fun reseed() {
        seed += 1
        random = SeededRandom(seed)
        running = false
        labLedger = Ledger()
        clearTable("New seed $seed. The score starts over.")
    }

    private fun clearTable(message: String) {
        ledger = Ledger()
        lastRound = null
        notice = message
    }

    companion object {
        /** Default stream. The same seed deals the same inputs on every target. */
        const val DEFAULT_SEED = 20261118L
    }
}
