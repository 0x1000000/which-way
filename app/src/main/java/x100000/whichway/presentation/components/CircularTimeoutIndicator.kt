package x100000.whichway.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity

@Composable
internal fun CircularTimeoutIndicator(
    progress: Animatable<Float, *>,
    uiMetrics: WatchUiMetrics,
    modifier: Modifier = Modifier,
) {
    val stroke = with(LocalDensity.current) { uiMetrics.indicatorStrokeWidth.toPx() }
    Canvas(modifier = modifier) {
        val inset = stroke
        drawArc(
            color = IndicatorTrackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2, size.height - inset * 2),
        )
        drawArc(
            color = IndicatorColor,
            startAngle = -90f,
            sweepAngle = 360f * progress.value.coerceIn(0f, 1f),
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2, size.height - inset * 2),
        )
    }
}
