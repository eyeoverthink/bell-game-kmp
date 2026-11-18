package dev.eyeoverthink.bellgame.ui

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PreloadTargetTest {
    @Test
    fun benchPreloadFillsTheBenchLedger() {
        assertTrue(preloadFillsBench(AppScreen.Bench))
    }

    @Test
    fun otherScreensPreloadTheTable() {
        assertFalse(preloadFillsBench(AppScreen.Table))
        assertFalse(preloadFillsBench(AppScreen.Ledger))
        assertFalse(preloadFillsBench(AppScreen.Causality))
        assertFalse(preloadFillsBench(AppScreen.Story))
    }
}
