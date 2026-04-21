package x100000.whichway.presentation

import android.graphics.Paint
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import x100000.whichway.game.Direction
import x100000.whichway.game.RoundData

@Composable
internal fun GameBoard(
    roundData: RoundData,
    roundNumber: Int,
    pressedDirection: Direction?,
    onDirectionTapped: (Direction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .pointerInput(roundData, roundNumber) {
                detectTapGestures { offset ->
                    onDirectionTapped(offset.toDirection(size.width.toFloat(), size.height.toFloat()))
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
    val topLeft = Offset.Zero
    val topRight = Offset(width, 0f)
    val bottomRight = Offset(width, height)
    val bottomLeft = Offset(0f, height)

    return mapOf(
        Direction.Up to Path().apply {
            moveTo(center.x, center.y)
            lineTo(topLeft.x, topLeft.y)
            lineTo(topRight.x, topRight.y)
            close()
        },
        Direction.Right to Path().apply {
            moveTo(center.x, center.y)
            lineTo(topRight.x, topRight.y)
            lineTo(bottomRight.x, bottomRight.y)
            close()
        },
        Direction.Down to Path().apply {
            moveTo(center.x, center.y)
            lineTo(bottomRight.x, bottomRight.y)
            lineTo(bottomLeft.x, bottomLeft.y)
            close()
        },
        Direction.Left to Path().apply {
            moveTo(center.x, center.y)
            lineTo(bottomLeft.x, bottomLeft.y)
            lineTo(topLeft.x, topLeft.y)
            close()
        },
    )
}
