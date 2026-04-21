package x100000.whichway.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import x100000.whichway.R
import x100000.whichway.data.DEFAULT_SPEED_PERCENT
import x100000.whichway.data.SavedGameData

@Composable
internal fun StatisticsScreen(
    savedData: SavedGameData,
    onBack: () -> Unit,
) {
    MenuScreen(
        title = stringResource(R.string.statistics),
        items = listOf(
            MenuItem.TextLine(
                text = labeledScoreValueWithSpeed(
                    label = stringResource(R.string.best_score),
                    score = savedData.bestScore,
                    speedPercent = savedData.bestScoreSpeedPercent,
                ),
            ),
            MenuItem.TextLine(
                text = labeledScoreValueWithSpeed(
                    label = stringResource(R.string.last_score),
                    score = savedData.lastScore,
                    speedPercent = savedData.lastScoreSpeedPercent,
                ),
            ),
            MenuItem.TextLine(
                text = stringResource(R.string.average_response_short_value, savedData.averageResponseTimeMs),
            ),
            MenuItem.TextLine(
                text = stringResource(
                    R.string.total_spent_time_value,
                    formatSpentTime(savedData.totalSpentTimeMs),
                ),
            ),
            MenuItem.TextLine(
                text = stringResource(R.string.games_played_value, savedData.gamesPlayed),
            ),
            MenuItem.Action(
                text = stringResource(R.string.home),
                onClick = onBack,
            ),
        ),
    )
}

@Composable
private fun labeledScoreValueWithSpeed(
    label: String,
    score: Int,
    speedPercent: Int,
): String = if (speedPercent == DEFAULT_SPEED_PERCENT) {
    "$label: $score"
} else {
    "$label: $score (${speedDeltaLabel(speedPercent)})"
}

internal fun speedDeltaLabel(speedPercent: Int): String =
    when {
        speedPercent > DEFAULT_SPEED_PERCENT -> "+${speedPercent - DEFAULT_SPEED_PERCENT}%"
        speedPercent < DEFAULT_SPEED_PERCENT -> "-${DEFAULT_SPEED_PERCENT - speedPercent}%"
        else -> "0%"
    }

internal fun formatSpentTime(totalMs: Long): String {
    val totalSeconds = (totalMs / 1000L).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes}m ${seconds}s"
}
