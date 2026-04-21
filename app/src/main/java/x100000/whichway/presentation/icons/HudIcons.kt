package x100000.whichway.presentation.icons

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp

@Composable
internal fun HudHeartIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val heartPath = buildHeartPath(width, height)

        val topColor = lerp(color, Color.White, 0.12f)
        val midColor = lerp(color, Color.White, 0.04f)
        val bottomColor = lerp(color, Color.Black, 0.14f)
        val rimColor = lerp(color, Color.Black, 0.28f).copy(alpha = 0.42f)
        val shadowColor = Color.Black.copy(alpha = 0.14f)

        drawPath(
            path = heartPath,
            brush = Brush.linearGradient(
                colors = listOf(topColor, midColor, bottomColor),
                start = Offset(width * 0.30f, height * 0.14f),
                end = Offset(width * 0.70f, height * 0.94f),
            ),
        )

        drawPath(
            path = heartPath,
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.24f), Color.Transparent),
                center = Offset(width * 0.38f, height * 0.26f),
                radius = width * 0.34f,
            ),
        )

        drawPath(
            path = heartPath,
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, shadowColor),
                center = Offset(width * 0.68f, height * 0.78f),
                radius = width * 0.55f,
            ),
        )

        drawPath(
            path = heartPath,
            color = rimColor,
            style = Stroke(width = width * 0.055f),
        )
    }
}

private fun buildHeartPath(
    width: Float,
    height: Float,
): Path = Path().apply {
    fillType = PathFillType.NonZero
    moveTo(width * 0.5f, height * 0.9f)
    cubicTo(width * 0.455f, height * 0.86f, width * 0.343f, height * 0.763f, width * 0.214f, height * 0.646f)
    cubicTo(width * 0.045f, height * 0.493f, width * 0.045f, height * 0.322f, width * 0.163f, height * 0.208f)
    cubicTo(width * 0.272f, height * 0.103f, width * 0.424f, height * 0.119f, width * 0.5f, height * 0.232f)
    cubicTo(width * 0.576f, height * 0.119f, width * 0.728f, height * 0.103f, width * 0.837f, height * 0.208f)
    cubicTo(width * 0.955f, height * 0.322f, width * 0.955f, height * 0.493f, width * 0.786f, height * 0.646f)
    cubicTo(width * 0.657f, height * 0.763f, width * 0.545f, height * 0.86f, width * 0.5f, height * 0.9f)
    close()
}
