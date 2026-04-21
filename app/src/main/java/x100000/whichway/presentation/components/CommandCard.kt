package x100000.whichway.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import kotlin.math.min

@Composable
internal fun CommandCard(
    prompt: String,
    hasZoneMarkers: Boolean,
    uiMetrics: WatchUiMetrics,
    modifier: Modifier = Modifier,
) {
    if (prompt.isBlank()) {
        return
    }
    val promptLines = remember(prompt) { prompt.lines() }
    val longestLineLength = remember(promptLines) { promptLines.maxOfOrNull { it.length } ?: 0 }
    val density = LocalDensity.current
    val promptLineHeightMultiplier = when {
        promptLines.size >= 3 -> 1.28
        promptLines.size == 2 -> 1.26
        else -> 1.08
    }
    val basePromptFontSize = when {
        hasZoneMarkers && longestLineLength >= 8 -> 12.sp
        hasZoneMarkers && longestLineLength >= 6 -> 14.sp
        hasZoneMarkers && (promptLines.size >= 3 || longestLineLength >= 18) -> 14.sp
        hasZoneMarkers && (promptLines.size >= 2 || longestLineLength >= 14) -> 16.sp
        hasZoneMarkers && (promptLines.size >= 2 || longestLineLength >= 10) -> 18.sp
        hasZoneMarkers -> 20.sp
        promptLines.size >= 3 || longestLineLength >= 18 -> 18.sp
        promptLines.size >= 2 || longestLineLength >= 14 -> 20.sp
        promptLines.size >= 2 || longestLineLength >= 10 -> 22.sp
        else -> 26.sp
    }
    val cappedMarkerFontScale = 1.15f
    val markerFontScaleAdjustment = if (hasZoneMarkers) {
        min(density.fontScale, cappedMarkerFontScale) / density.fontScale
    } else {
        1f
    }
    val promptFontSize = (basePromptFontSize.value * uiMetrics.promptFontScale * markerFontScaleAdjustment).sp
    Box(
        modifier = modifier.padding(
            horizontal = uiMetrics.promptHorizontalPadding,
            vertical = uiMetrics.promptVerticalPadding,
        ),
    ) {
        Text(
            text = prompt,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            softWrap = false,
            maxLines = 3,
            overflow = TextOverflow.Visible,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = promptFontSize,
                lineHeight = promptFontSize * promptLineHeightMultiplier,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.9f),
                    offset = Offset(0f, with(density) { uiMetrics.promptShadowOffsetY.toPx() }),
                    blurRadius = with(density) { uiMetrics.promptShadowBlur.toPx() },
                ),
            ),
            color = Color.White,
        )
    }
}
