package dev.eyeoverthink.bellgame.ui

import androidx.compose.ui.graphics.Color
import dev.eyeoverthink.bellgame.engine.Regime

/**
 * The night-observatory palette. Brass is the local ceiling, ion is the
 * quantum curve, and alarm is the PR box.
 */
object BellColors {
    val Night = Color(0xFF07141C)
    val Panel = Color(0xFF102431)
    val Line = Color(0xFF1E3C4A)
    val Ink = Color(0xFFE7F4F2)
    val Muted = Color(0xFF8EAEB3)
    val Ion = Color(0xFF5CE1FF)
    val Brass = Color(0xFFE2B15A)
    val Alarm = Color(0xFFFF5C8A)
    val Win = Color(0xFF8DFFC1)
    val Miss = Color(0xFFFF8B7A)
}

/** Color used to mark a [Regime]. */
fun regimeColor(regime: Regime): Color = when (regime) {
    Regime.Classical -> BellColors.Brass
    Regime.Quantum -> BellColors.Ion
    Regime.PopescuRohrlich -> BellColors.Alarm
}
