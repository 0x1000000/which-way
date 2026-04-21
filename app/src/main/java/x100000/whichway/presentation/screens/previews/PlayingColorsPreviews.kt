package x100000.whichway.presentation

import androidx.compose.runtime.Composable
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import x100000.whichway.game.ColorTarget
import x100000.whichway.game.Direction
import x100000.whichway.game.GameConfig
import x100000.whichway.game.GameScreenState
import x100000.whichway.game.RoundData
import x100000.whichway.game.ZoneFacts
import x100000.whichway.presentation.theme.WhichWayTheme

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun PlayingPreview() {
    WhichWayTheme {
        PlayingScreen(
            state = GameScreenState.Playing(
                score = 9,
                lives = 2,
                charges = 1,
                mistakes = 1,
                roundNumber = 4,
                isRestartRound = false,
                config = GameConfig(unlockFloor = 31, speedPercent = 100),
                roundData = RoundData(
                    prompt = ColorTarget.Yellow.label,
                    validDirections = setOf(Direction.Right),
                    zoneFacts = mapOf(
                        Direction.Up to ZoneFacts(color = ColorTarget.Green),
                        Direction.Right to ZoneFacts(color = ColorTarget.Blue),
                        Direction.Down to ZoneFacts(color = ColorTarget.White),
                        Direction.Left to ZoneFacts(color = ColorTarget.Yellow),
                    ),
                ),
            ),
            isPaused = false,
            onContinue = {},
            onExit = {},
            onResolve = { _, _, _ -> null },
            onBusyStateChanged = {},
            onApplyTransition = {},
        )
    }
}
