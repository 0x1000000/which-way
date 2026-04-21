package x100000.whichway.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import x100000.whichway.game.ColorTarget
import x100000.whichway.game.Direction
import x100000.whichway.game.RoundData
import x100000.whichway.game.ZoneFacts
import x100000.whichway.presentation.theme.WhichWayTheme

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun PlayingFlashPreview() {
    WhichWayTheme {
        val uiMetrics = rememberWatchUiMetrics()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor),
        ) {
            GameBoard(
                roundData = RoundData(
                    prompt = "LEFT",
                    validDirections = setOf(Direction.Left),
                    zoneFacts = mapOf(
                        Direction.Up to ZoneFacts(color = ColorTarget.Green),
                        Direction.Right to ZoneFacts(color = ColorTarget.Blue),
                        Direction.Down to ZoneFacts(color = ColorTarget.White),
                        Direction.Left to ZoneFacts(color = ColorTarget.Yellow),
                    ),
                ),
                roundNumber = 1,
                pressedDirection = Direction.Right,
                onDirectionTapped = {},
                modifier = Modifier.fillMaxSize(),
            )
            FlashOverlay(
                flashColor = ReplayFlashColor,
                flashSymbol = "\u21BB",
                flashSymbolColor = FlashAccentTextColor,
                symbolFontSize = uiMetrics.feedbackSymbolFontSize,
                animationProgress = 1f,
            )
        }
    }
}
