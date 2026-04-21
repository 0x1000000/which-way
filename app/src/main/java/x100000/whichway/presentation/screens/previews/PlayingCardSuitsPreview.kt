package x100000.whichway.presentation

import androidx.compose.runtime.Composable
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import x100000.whichway.game.Direction
import x100000.whichway.game.GameConfig
import x100000.whichway.game.GameScreenState
import x100000.whichway.game.RoundData
import x100000.whichway.game.SuitTarget
import x100000.whichway.game.ZoneFacts
import x100000.whichway.presentation.theme.WhichWayTheme

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun PlayingCardSuitsPreview() {
    WhichWayTheme {
        PlayingScreen(
            state = GameScreenState.Playing(
                score = 24,
                lives = 3,
                charges = 2,
                mistakes = 0,
                roundNumber = 11,
                isRestartRound = false,
                config = GameConfig(unlockFloor = 0, speedPercent = 100),
                roundData = RoundData(
                    prompt = SuitTarget.Diamonds.label,
                    validDirections = setOf(Direction.Right),
                    zoneFacts = mapOf(
                        Direction.Up to ZoneFacts(suit = SuitTarget.Clubs),
                        Direction.Right to ZoneFacts(suit = SuitTarget.Diamonds),
                        Direction.Down to ZoneFacts(suit = SuitTarget.Hearts),
                        Direction.Left to ZoneFacts(suit = SuitTarget.Spades),
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
