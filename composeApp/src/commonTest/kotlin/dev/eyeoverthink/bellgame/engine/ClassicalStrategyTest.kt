package dev.eyeoverthink.bellgame.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClassicalStrategyTest {
    @Test
    fun everyDeterministicTableIsListedOnce() {
        val tables = LocalTable.all()
        assertEquals(16, tables.size)
        assertEquals(tables.size, tables.toSet().size)
    }

    @Test
    fun bestLocalWinRateIsExactlyThreeQuarters() {
        val rates = LocalTable.all().map { it.exactWinRate() }
        assertEquals(ChshConstants.CLASSICAL_WIN_RATE, rates.max(), 0.0)
        assertEquals(ChshConstants.CLASSICAL_WIN_RATE, LocalTable.bestWinRate(), 0.0)
        assertTrue(rates.all { it <= ChshConstants.CLASSICAL_WIN_RATE })
        assertTrue(rates.any { it == ChshConstants.CLASSICAL_WIN_RATE })
    }

    @Test
    fun constantOutputsWinThreeDealsAndMissTheAnd() {
        val table = LocalTable.alwaysZero
        assertTrue(table.winsInput(0, 0))
        assertTrue(table.winsInput(0, 1))
        assertTrue(table.winsInput(1, 0))
        assertTrue(!table.winsInput(1, 1))
        assertEquals(ChshConstants.CLASSICAL_WIN_RATE, table.exactWinRate(), 0.0)
    }

    @Test
    fun sharedRandomnessIsOnlyAnAverageOfTables() {
        val rates = LocalTable.all().map { it.exactWinRate() }
        val mixed = rates.average()
        assertTrue(mixed <= LocalTable.bestWinRate())
        val weighted = 0.2 * rates[0] + 0.5 * rates[7] + 0.3 * rates[15]
        assertTrue(weighted <= ChshConstants.CLASSICAL_WIN_RATE)
    }
}
