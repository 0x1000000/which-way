package x100000.whichway.presentation.icons

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import x100000.whichway.game.SuitTarget

internal fun DrawScope.drawSuitIcon(
    suit: SuitTarget,
    center: Offset,
    radius: Float,
) {
    when (suit) {
        SuitTarget.Diamonds -> drawDiamondIcon(center, radius)
        SuitTarget.Hearts -> drawHeartSuitIcon(center, radius)
        SuitTarget.Spades -> drawSpadeIcon(center, radius)
        SuitTarget.Clubs -> drawClubIcon(center, radius)
    }
}

private fun DrawScope.drawDiamondIcon(
    center: Offset,
    radius: Float,
) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x + radius * 0.78f, center.y)
        lineTo(center.x, center.y + radius)
        lineTo(center.x - radius * 0.78f, center.y)
        close()
    }
    drawGlossySuitPath(
        path = path,
        center = center,
        radius = radius,
        baseColor = Color(0xFFFF6B6B),
    )
}

private fun DrawScope.drawHeartSuitIcon(
    center: Offset,
    radius: Float,
) {
    val width = radius * 1.85f
    val height = radius * 1.72f
    val left = center.x - width / 2f
    val top = center.y - height * 0.52f
    val path = Path().apply {
        fillType = PathFillType.NonZero
        moveTo(left + width * 0.5f, top + height * 0.90f)
        cubicTo(left + width * 0.455f, top + height * 0.86f, left + width * 0.343f, top + height * 0.763f, left + width * 0.214f, top + height * 0.646f)
        cubicTo(left + width * 0.045f, top + height * 0.493f, left + width * 0.045f, top + height * 0.322f, left + width * 0.163f, top + height * 0.208f)
        cubicTo(left + width * 0.272f, top + height * 0.103f, left + width * 0.424f, top + height * 0.119f, left + width * 0.5f, top + height * 0.232f)
        cubicTo(left + width * 0.576f, top + height * 0.119f, left + width * 0.728f, top + height * 0.103f, left + width * 0.837f, top + height * 0.208f)
        cubicTo(left + width * 0.955f, top + height * 0.322f, left + width * 0.955f, top + height * 0.493f, left + width * 0.786f, top + height * 0.646f)
        cubicTo(left + width * 0.657f, top + height * 0.763f, left + width * 0.545f, top + height * 0.86f, left + width * 0.5f, top + height * 0.9f)
        close()
    }
    drawGlossySuitPath(
        path = path,
        center = center,
        radius = radius,
        baseColor = Color(0xFFFF7474),
    )
}

private fun DrawScope.drawSpadeIcon(
    center: Offset,
    radius: Float,
) {
    val width = radius * 1.74f
    val height = radius * 1.82f
    val left = center.x - width / 2f
    val top = center.y - height * 0.58f
    val headPath = Path().apply {
        fillType = PathFillType.NonZero
        moveTo(left + width * 0.5f, top + height * 0.03f)
        cubicTo(
            left + width * 0.67f, top + height * 0.19f,
            left + width * 0.88f, top + height * 0.37f,
            left + width * 0.88f, top + height * 0.58f,
        )
        cubicTo(
            left + width * 0.88f, top + height * 0.77f,
            left + width * 0.75f, top + height * 0.89f,
            left + width * 0.58f, top + height * 0.89f,
        )
        cubicTo(
            left + width * 0.54f, top + height * 0.89f,
            left + width * 0.50f, top + height * 0.86f,
            left + width * 0.50f, top + height * 0.81f,
        )
        cubicTo(
            left + width * 0.50f, top + height * 0.86f,
            left + width * 0.46f, top + height * 0.89f,
            left + width * 0.42f, top + height * 0.89f,
        )
        cubicTo(
            left + width * 0.25f, top + height * 0.89f,
            left + width * 0.12f, top + height * 0.77f,
            left + width * 0.12f, top + height * 0.58f,
        )
        cubicTo(
            left + width * 0.12f, top + height * 0.37f,
            left + width * 0.33f, top + height * 0.19f,
            left + width * 0.50f, top + height * 0.03f,
        )
        close()
    }
    val stemPath = Path().apply {
        moveTo(center.x - radius * 0.14f, center.y + radius * 0.50f)
        cubicTo(
            center.x - radius * 0.10f, center.y + radius * 0.79f,
            center.x - radius * 0.27f, center.y + radius * 0.99f,
            center.x - radius * 0.41f, center.y + radius * 1.10f,
        )
        lineTo(center.x + radius * 0.41f, center.y + radius * 1.10f)
        cubicTo(
            center.x + radius * 0.27f, center.y + radius * 0.99f,
            center.x + radius * 0.10f, center.y + radius * 0.79f,
            center.x + radius * 0.14f, center.y + radius * 0.50f,
        )
        close()
    }
    drawGlossySuitPath(
        path = headPath,
        center = Offset(center.x, center.y - radius * 0.08f),
        radius = radius,
        baseColor = Color(0xFF151A20),
    )
    drawGlossySuitPath(
        path = stemPath,
        center = center,
        radius = radius * 0.72f,
        baseColor = Color(0xFF151A20),
    )
}

private fun DrawScope.drawClubIcon(
    center: Offset,
    radius: Float,
) {
    val path = Path().apply {
        fillType = PathFillType.NonZero
        addOval(
            Rect(
                center = Offset(center.x, center.y - radius * 0.42f),
                radius = radius * 0.44f,
            ),
        )
        addOval(
            Rect(
                center = Offset(center.x - radius * 0.40f, center.y + radius * 0.02f),
                radius = radius * 0.42f,
            ),
        )
        addOval(
            Rect(
                center = Offset(center.x + radius * 0.40f, center.y + radius * 0.02f),
                radius = radius * 0.42f,
            ),
        )
        moveTo(center.x - radius * 0.22f, center.y + radius * 0.18f)
        cubicTo(
            center.x - radius * 0.18f, center.y + radius * 0.54f,
            center.x - radius * 0.42f, center.y + radius * 0.86f,
            center.x - radius * 0.60f, center.y + radius * 1.00f,
        )
        lineTo(center.x + radius * 0.60f, center.y + radius * 1.00f)
        cubicTo(
            center.x + radius * 0.42f, center.y + radius * 0.86f,
            center.x + radius * 0.18f, center.y + radius * 0.54f,
            center.x + radius * 0.22f, center.y + radius * 0.18f,
        )
        close()
    }
    drawGlossySuitPath(
        path = path,
        center = center,
        radius = radius,
        baseColor = Color(0xFF151A20),
        drawRim = false,
    )
}

private fun DrawScope.drawGlossySuitPath(
    path: Path,
    center: Offset,
    radius: Float,
    baseColor: Color,
    drawRim: Boolean = true,
) {
    val topColor = lerp(baseColor, Color.White, 0.16f)
    val midColor = lerp(baseColor, Color.White, 0.05f)
    val bottomColor = lerp(baseColor, Color.Black, 0.18f)
    val rimColor = lerp(baseColor, Color.Black, 0.30f).copy(alpha = 0.36f)

    drawPath(
        path = path,
        brush = Brush.linearGradient(
            colors = listOf(topColor, midColor, bottomColor),
            start = Offset(center.x - radius * 0.65f, center.y - radius * 0.95f),
            end = Offset(center.x + radius * 0.58f, center.y + radius * 1.05f),
        ),
    )
    drawPath(
        path = path,
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
            center = Offset(center.x - radius * 0.22f, center.y - radius * 0.38f),
            radius = radius * 0.88f,
        ),
    )
    if (drawRim) {
        drawPath(
            path = path,
            color = rimColor,
            style = Stroke(width = radius * 0.11f),
        )
    }
}

private fun Rect(
    center: Offset,
    radius: Float,
): Rect = Rect(
    left = center.x - radius,
    top = center.y - radius,
    right = center.x + radius,
    bottom = center.y + radius,
)
