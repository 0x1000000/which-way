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
private fun PlayingComplexLogicPreview() {
    WhichWayTheme {
        PlayingScreen(
            state = GameScreenState.Playing(
                score = 46,
                lives = 1,
                charges = 1,
                mistakes = 2,
                roundNumber = 19,
                isRestartRound = false,
                config = GameConfig(unlockFloor = 0, speedPercent = 100),
                roundData = RoundData(
                    prompt = "NOT\u00A0GREEN\nAND\nNOT\u00A0UP",
                    validDirections = setOf(Direction.Down, Direction.Left),
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
