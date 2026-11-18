package dev.eyeoverthink.bellgame.ui

/**
 * Screens of the exhibit. A token is accepted from the desktop environment
 * or from the browser hash so a screenshot harness can open a specific page.
 */
enum class AppScreen(val label: String) {
    Table("Table"),
    Bench("Bench"),
    Causality("Causality"),
    Ledger("Ledger"),
    Story("Story"),
    ;

    companion object {
        /** Map a short token onto a screen. Unknown tokens open the table. */
        fun fromToken(token: String?): AppScreen = when (token?.lowercase()?.removePrefix("#")) {
            "bench", "lab" -> Bench
            "causality", "cause" -> Causality
            "ledger", "stats" -> Ledger
            "story", "about" -> Story
            else -> Table
        }
    }
}
