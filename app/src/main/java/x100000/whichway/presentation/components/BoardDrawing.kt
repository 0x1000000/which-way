package x100000.whichway.presentation

import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import x100000.whichway.game.Direction
import x100000.whichway.game.RoundData
import x100000.whichway.presentation.icons.drawSuitIcon

internal fun DrawScope.drawDirectionalZones(
    roundData: RoundData,
    pressedDirection: Direction?,
    zonePaths: Map<Direction, androidx.compose.ui.graphics.Path>,
    numberTextPaint: Paint,
    targetTextPaint: Paint,
) {
    val center = Offset(size.width / 2f, size.height / 2f)

    zonePaths.forEach { (direction, path) ->
        val baseColor = ZoneBaseColors.getValue(direction)
        val isPressed = pressedDirection == direction
        drawPath(
            path = path,
            color = if (isPressed) ZonePressedColor else baseColor,
            style = Fill,
        )
        drawPath(
            path = path,
            color = Color(0x1FFFFFFF),
            style = Stroke(width = 2f),
        )
    }

    roundData.zoneFacts.forEach { (direction, facts) ->
        if (facts.color != null || facts.number != null || facts.suit != null || facts.target) {
            val markerCenter = zoneCenter(direction, center)
            val markerRadius = size.minDimension * 0.07f
            val isBlackSuit = facts.suit == x100000.whichway.game.SuitTarget.Clubs || facts.suit == x100000.whichway.game.SuitTarget.Spades
            val textColor = when {
                facts.suit != null && isBlackSuit -> android.graphics.Color.BLACK
                facts.color == x100000.whichway.game.ColorTarget.White || facts.color == x100000.whichway.game.ColorTarget.Yellow -> android.graphics.Color.BLACK
                else -> android.graphics.Color.WHITE
            }
            drawCircle(
                color = when {
                    facts.target -> Color(0xCC11161C)
                    facts.color != null -> facts.color.toUiColor()
                    facts.suit != null && isBlackSuit -> Color(0xFFE9EEF5)
                    facts.suit != null -> Color(0xCC11161C)
                    else -> Color(0xAA11161C)
                },
                radius = markerRadius,
                center = markerCenter,
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.2f),
                radius = markerRadius,
                center = markerCenter,
                style = Stroke(width = size.minDimension * 0.01f),
            )
            if (facts.target) {
                val baseline = markerCenter.y - (targetTextPaint.descent() + targetTextPaint.ascent()) / 2f
                drawContext.canvas.nativeCanvas.drawText(
                    "!",
                    markerCenter.x,
                    baseline,
                    targetTextPaint,
                )
            }
            if (facts.suit != null) {
                drawSuitIcon(
                    suit = facts.suit,
                    center = markerCenter,
                    radius = if (facts.number != null) markerRadius * 0.52f else markerRadius * 0.805f,
                )
            }
            if (facts.number != null) {
                val text = facts.number.toString()
                val textBounds = Rect().also { bounds ->
                    numberTextPaint.getTextBounds(text, 0, text.length, bounds)
                }
                val previousColor = numberTextPaint.color
                val previousAlign = numberTextPaint.textAlign
                numberTextPaint.color = textColor
                numberTextPaint.textAlign = Paint.Align.LEFT
                val textX = markerCenter.x - textBounds.exactCenterX()
                val baseline = markerCenter.y - textBounds.exactCenterY()
                drawContext.canvas.nativeCanvas.drawText(
                    text,
                    textX,
                    baseline,
                    numberTextPaint,
                )
                numberTextPaint.textAlign = previousAlign
                numberTextPaint.color = previousColor
            }
        }
    }
}

internal fun DrawScope.zoneCenter(
    direction: Direction,
    center: Offset,
): Offset {
    val distance = size.minDimension * 0.28f
    return when (direction) {
        Direction.Up -> Offset(center.x, center.y - distance)
        Direction.Down -> Offset(center.x, center.y + distance)
        Direction.Left -> Offset(center.x - distance, center.y)
        Direction.Right -> Offset(center.x + distance, center.y)
    }
}
