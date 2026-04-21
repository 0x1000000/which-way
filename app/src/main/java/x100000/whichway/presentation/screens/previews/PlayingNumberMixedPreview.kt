package x100000.whichway.presentation

import androidx.compose.runtime.Composable
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import x100000.whichway.game.ColorTarget
import x100000.whichway.game.Direction
import x100000.whichway.game.GameConfig
import x100000.whichway.game.GameCommand
import x100000.whichway.game.GameScreenState
import x100000.whichway.game.RoundData
import x100000.whichway.game.ZoneFacts
import x100000.whichway.presentation.theme.WhichWayTheme

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun PlayingColorNumberMixedPreview() {
    WhichWayTheme {
        PlayingScreen(
            state = GameScreenState.Playing(
                score = 101,
                lives = 2,
                charges = 1,
                mistakes = 1,
                roundNumber = 24,
                isRestartRound = false,
                config = GameConfig(unlockFloor = 100, speedPercent = 100),
                roundData = RoundData(
                    prompt = "BLUE\nOR\n7",
                    validDirections = setOf(Direction.Up, Direction.Right),
                    zoneFacts = mapOf(
                        Direction.Up to ZoneFacts(color = ColorTarget.Blue, number = 12),
                        Direction.Right to ZoneFacts(color = ColorTarget.Green, number = 7),
                        Direction.Down to ZoneFacts(color = ColorTarget.White, number = 10),
                        Direction.Left to ZoneFacts(color = ColorTarget.Yellow, number = 15),
                    ),
                    commandId = GameCommand.BLUE_OR_NUMBER,
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

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun PlayingDirectionNumberMixedPreview() {
    WhichWayTheme {
        PlayingScreen(
            state = GameScreenState.Playing(
                score = 96,
                lives = 2,
                charges = 2,
                mistakes = 0,
                roundNumber = 23,
                isRestartRound = false,
                config = GameConfig(unlockFloor = 95, speedPercent = 100),
                roundData = RoundData(
                    prompt = "UP\nOR\n7",
                    validDirections = setOf(Direction.Up, Direction.Left),
                    zoneFacts = mapOf(
                        Direction.Up to ZoneFacts(number = 14),
                        Direction.Right to ZoneFacts(number = 11),
                        Direction.Down to ZoneFacts(number = 16),
                        Direction.Left to ZoneFacts(number = 7),
                    ),
                    commandId = GameCommand.UP_OR_NUMBER,
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
