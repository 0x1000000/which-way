package x100000.whichway.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import x100000.whichway.R
import x100000.whichway.data.DEFAULT_SPEED_PERCENT
import x100000.whichway.game.GameScreenState

@Composable
internal fun GameOverScreen(
    state: GameScreenState.GameOver,
    bestScore: Int,
    bestScoreSpeedPercent: Int,
    averageResponseTimeMs: Int,
    sessionSpentTimeMs: Long,
    onRestart: () -> Unit,
    onBackToStart: () -> Unit,
    initialAnchorItemIndex: Int = 1,
) {
    val items = buildList {
        if (state.newRecord) {
            add(
                MenuItem.Badge(
                    text = stringResource(R.string.new_record),
                    backgroundColor = Color(0xFF1A3C29),
                    contentColor = Color(0xFFB8FFCA),
                ),
            )
        }
        add(
            MenuItem.SummaryCard(
                title = stringResource(R.string.score_with_value, state.score),
                value = "",
                supportingLines = listOf(
                    bestScoreLine(bestScore, bestScoreSpeedPercent),
                    stringResource(R.string.average_response_short_value, averageResponseTimeMs),
                    stringResource(R.string.session_spent_time_value, formatSpentTime(sessionSpentTimeMs)),
                ),
            ),
        )
        add(
            MenuItem.Action(
                text = stringResource(R.string.restart),
                onClick = onRestart,
            ),
        )
        add(
            MenuItem.Action(
                text = stringResource(R.string.home),
                onClick = onBackToStart,
            ),
        )
    }

    MenuScreen(
        title = stringResource(R.string.game_over),
        items = items,
        initialAnchorItemIndex = initialAnchorItemIndex,
    )
}

@Composable
private fun bestScoreLine(
    bestScore: Int,
    bestScoreSpeedPercent: Int,
): String = if (bestScoreSpeedPercent == DEFAULT_SPEED_PERCENT) {
    stringResource(R.string.best_score_value, bestScore)
} else {
    stringResource(R.string.best_score_with_speed_value, bestScore, speedDeltaLabel(bestScoreSpeedPercent))
}
