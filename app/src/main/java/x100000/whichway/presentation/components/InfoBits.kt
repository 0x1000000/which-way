package x100000.whichway.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import x100000.whichway.game.GameRules

@Composable
internal fun ScoreLine(
    label: String,
    value: Int,
    uiMetrics: WatchUiMetrics,
) {
    ValueLine(label = label, value = value.toString(), uiMetrics = uiMetrics)
}

@Composable
internal fun ValueLine(
    label: String,
    value: String,
    uiMetrics: WatchUiMetrics,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = Color(0xFFC4CBD4),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = uiMetrics.scoreLabelFontSize),
        )
        Spacer(modifier = Modifier.width(uiMetrics.smallGap))
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = uiMetrics.scoreValueFontSize),
        )
    }
}

@Composable
internal fun LivesRow(
    lives: Int,
    uiMetrics: WatchUiMetrics,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(uiMetrics.smallGap),
    ) {
        repeat(GameRules.STARTING_LIVES) { index ->
            val alpha by animateFloatAsState(
                targetValue = if (index < lives) 1f else 0.25f,
                animationSpec = tween(durationMillis = 180),
                label = "life_alpha_$index",
            )
            Text(
                text = "\u2665",
                color = if (index < lives) LifeOnColor.copy(alpha = alpha) else LifeOffColor.copy(alpha = alpha),
                fontSize = uiMetrics.hudLifeFontSize,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}
