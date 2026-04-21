package x100000.whichway.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val BASELINE_WATCH_DIAMETER_DP = 192f

@Immutable
internal data class WatchUiMetrics(
    val diameterDp: Dp,
    val screenPaddingHorizontal: Dp,
    val screenPaddingVertical: Dp,
    val smallGap: Dp,
    val mediumGap: Dp,
    val largeGap: Dp,
    val titleFontSize: TextUnit,
    val primaryButtonFontSize: TextUnit,
    val secondaryButtonFontSize: TextUnit,
    val scoreLabelFontSize: TextUnit,
    val scoreValueFontSize: TextUnit,
    val promptHorizontalPadding: Dp,
    val promptVerticalPadding: Dp,
    val promptFontScale: Float,
    val promptShadowOffsetY: Dp,
    val promptShadowBlur: Dp,
    val hudLifeFontSize: TextUnit,
    val hudChargeFontSize: TextUnit,
    val hudInfoFontSize: TextUnit,
    val hudChargeSpacingDegrees: Float,
    val indicatorStrokeWidth: Dp,
    val feedbackSymbolFontSize: TextUnit,
    val exitCardCornerRadius: Dp,
    val exitCardPaddingHorizontal: Dp,
    val exitCardPaddingVertical: Dp,
    val newRecordPaddingHorizontal: Dp,
    val newRecordPaddingVertical: Dp,
)

@Composable
internal fun rememberWatchUiMetrics(screenDiameter: Dp): WatchUiMetrics =
    remember(screenDiameter) {
        val scale = (screenDiameter.value / BASELINE_WATCH_DIAMETER_DP).coerceIn(0.84f, 1.22f)
        val promptScale = (screenDiameter.value / BASELINE_WATCH_DIAMETER_DP).coerceIn(0.88f, 1.2f)

        fun scaledDp(base: Float): Dp = (base * scale).dp
        fun scaledSp(base: Float): TextUnit = (base * scale).sp

        WatchUiMetrics(
            diameterDp = screenDiameter,
            screenPaddingHorizontal = scaledDp(18f),
            screenPaddingVertical = scaledDp(14f),
            smallGap = scaledDp(6f),
            mediumGap = scaledDp(8f),
            largeGap = scaledDp(12f),
            titleFontSize = scaledSp(20f),
            primaryButtonFontSize = scaledSp(20f),
            secondaryButtonFontSize = scaledSp(15f),
            scoreLabelFontSize = scaledSp(12f),
            scoreValueFontSize = scaledSp(14f),
            promptHorizontalPadding = scaledDp(18f),
            promptVerticalPadding = scaledDp(10f),
            promptFontScale = promptScale,
            promptShadowOffsetY = scaledDp(3f),
            promptShadowBlur = scaledDp(12f),
            hudLifeFontSize = scaledSp(14f),
            hudChargeFontSize = scaledSp(14.4f),
            hudInfoFontSize = scaledSp(12f),
            hudChargeSpacingDegrees = (12f * scale).coerceIn(10f, 15f),
            indicatorStrokeWidth = scaledDp(4f),
            feedbackSymbolFontSize = scaledSp(34f),
            exitCardCornerRadius = scaledDp(18f),
            exitCardPaddingHorizontal = scaledDp(16f),
            exitCardPaddingVertical = scaledDp(14f),
            newRecordPaddingHorizontal = scaledDp(12f),
            newRecordPaddingVertical = scaledDp(4f),
        )
    }

@Composable
internal fun rememberWatchUiMetrics(): WatchUiMetrics {
    val configuration = LocalConfiguration.current
    val screenDiameter = minOf(configuration.screenWidthDp, configuration.screenHeightDp).dp
    return rememberWatchUiMetrics(screenDiameter)
}
