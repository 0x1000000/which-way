package x100000.whichway.presentation

import androidx.compose.runtime.Composable
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import x100000.whichway.game.GameConfig
import x100000.whichway.game.GameScreenState
import x100000.whichway.presentation.theme.WhichWayTheme

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun GameOverPreview() {
    WhichWayTheme {
        GameOverScreen(
            state = GameScreenState.GameOver(
                score = 27,
                newRecord = true,
                config = GameConfig(),
            ),
            bestScore = 98,
            bestScoreSpeedPercent = 125,
            averageResponseTimeMs = 842,
            sessionSpentTimeMs = 93_000L,
            initialAnchorItemIndex = 0,
            onRestart = {},
            onBackToStart = {},
        )
    }
}
