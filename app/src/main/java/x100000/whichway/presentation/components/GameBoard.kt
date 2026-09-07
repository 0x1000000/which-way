package x100000.whichway.presentation

import android.graphics.Paint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import x100000.whichway.game.Direction
import x100000.whichway.game.RoundData
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
internal fun GameBoard(
    roundData: RoundData,
    roundNumber: Int,
    pressedDirection: Direction?,
    onDirectionTapped: (Direction) -> Unit,
    modifier: Modifier = Modifier,
    highlightDirections: Set<Direction> = emptySet(),
    highlightAlpha: Float = 0.42f,
) {
    Box(
        modifier = modifier
            .pointerInput(roundData, roundNumber) {
                awaitPointerEventScope {
                    while (true) {
                        val downEvent = awaitPointerEvent(PointerEventPass.Initial)
                        val downChange = downEvent.changes.firstOrNull { it.pressed && !it.previousPressed }
                            ?: continue
                        onDirectionTapped(
                            downChange.position.toDirection(size.width.toFloat(), size.height.toFloat()),
                        )
                        downChange.consume()

                        do {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            event.changes.forEach { change ->
                                if (change.pressed && change.positionChanged()) {
                                    change.consume()
                                }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
            }
            .drawWithCache {
                val center = Offset(size.width / 2f, size.height / 2f)
                val zonePaths = createDirectionalZonePaths(
                    width = size.width,
                    height = size.height,
                    center = center,
                )
                val numberTextPaint = Paint().apply {
                    color = android.graphics.Color.WHITE
                    textAlign = Paint.Align.CENTER
                    textSize = size.minDimension * 0.09f
                    isFakeBoldText = true
                    isAntiAlias = true
                }
                val targetTextPaint = Paint().apply {
                    color = android.graphics.Color.WHITE
                    textAlign = Paint.Align.CENTER
                    textSize = size.minDimension * 0.13f
                    isFakeBoldText = true
                    isAntiAlias = true
                    setShadowLayer(size.minDimension * 0.025f, 0f, size.minDimension * 0.01f, android.graphics.Color.BLACK)
                }
                onDrawBehind {
                    drawDirectionalZones(
                        roundData = roundData,
                        pressedDirection = pressedDirection,
                        highlightDirections = highlightDirections,
                        highlightAlpha = highlightAlpha,
                        zonePaths = zonePaths,
                        numberTextPaint = numberTextPaint,
                        targetTextPaint = targetTextPaint,
                    )
                }
            }
            .fillMaxSize(),
    )
}

internal fun Offset.toDirection(
    width: Float,
    height: Float,
): Direction {
    val dx = x - width / 2f
    val dy = y - height / 2f
    return if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
        if (dx >= 0f) Direction.Right else Direction.Left
    } else {
        if (dy >= 0f) Direction.Down else Direction.Up
    }
}

private fun createDirectionalZonePaths(
    width: Float,
    height: Float,
    center: Offset,
): Map<Direction, Path> {
    val radius = min(width, height) / 2f
    val circleBounds = Rect(
        left = center.x - radius,
        top = center.y - radius,
        right = center.x + radius,
        bottom = center.y + radius,
    )

    fun sector(startAngle: Float): Path {
        val radians = Math.toRadians(startAngle.toDouble())
        val arcStart = Offset(
            x = center.x + radius * cos(radians).toFloat(),
            y = center.y + radius * sin(radians).toFloat(),
        )
        return Path().apply {
            moveTo(center.x, center.y)
            lineTo(arcStart.x, arcStart.y)
            arcTo(circleBounds, startAngle, 90f, forceMoveTo = false)
            close()
        }
    }

    return mapOf(
        Direction.Up to sector(225f),
        Direction.Right to sector(315f),
        Direction.Down to sector(45f),
        Direction.Left to sector(135f),
    )
}
