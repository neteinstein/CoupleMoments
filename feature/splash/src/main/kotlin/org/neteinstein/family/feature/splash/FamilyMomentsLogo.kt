package org.neteinstein.family.feature.splash

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate

@Composable
fun FamilyMomentsLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension / 2f

        // Background circle
        drawCircle(color = Color(0xFFFFDDB5), radius = r)

        // Outer ring
        drawCircle(
            color = Color(0xFFBF6900),
            radius = r,
            style = Stroke(width = r * 0.04f)
        )

        // Heart shape representing family love
        drawHeart(cx, cy, r * 0.38f, Color(0xFFBF6900))

        // Three figures (parent + parent + child)
        val figureBaseY = cy + r * 0.18f
        drawFigure(cx - r * 0.30f, figureBaseY, r * 0.14f, Color(0xFF6F5B40)) // left parent
        drawFigure(cx + r * 0.30f, figureBaseY, r * 0.14f, Color(0xFF6F5B40)) // right parent
        drawFigure(cx, figureBaseY + r * 0.04f, r * 0.10f, Color(0xFFBF6900)) // child (smaller)

        // Stars / sparkles
        drawSparkle(cx - r * 0.6f, cy - r * 0.5f, r * 0.06f, Color(0xFFFFB95A))
        drawSparkle(cx + r * 0.6f, cy - r * 0.5f, r * 0.06f, Color(0xFFFFB95A))
        drawSparkle(cx, cy - r * 0.72f, r * 0.05f, Color(0xFFFFB95A))
    }
}

private fun DrawScope.drawHeart(cx: Float, cy: Float, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(cx, cy + size * 0.35f)
        cubicTo(cx - size * 1.2f, cy - size * 0.3f, cx - size * 1.4f, cy - size, cx, cy - size * 0.55f)
        cubicTo(cx + size * 1.4f, cy - size, cx + size * 1.2f, cy - size * 0.3f, cx, cy + size * 0.35f)
        close()
    }
    drawPath(path = path, color = color.copy(alpha = 0.25f))
    drawPath(path = path, color = color, style = Stroke(width = size * 0.1f))
}

private fun DrawScope.drawFigure(x: Float, baseY: Float, size: Float, color: Color) {
    // Head
    drawCircle(color = color, radius = size * 0.35f, center = Offset(x, baseY - size * 1.1f))
    // Body
    val bodyPath = Path().apply {
        moveTo(x - size * 0.3f, baseY - size * 0.7f)
        lineTo(x - size * 0.25f, baseY)
        lineTo(x + size * 0.25f, baseY)
        lineTo(x + size * 0.3f, baseY - size * 0.7f)
        close()
    }
    drawPath(bodyPath, color = color.copy(alpha = 0.85f))
}

private fun DrawScope.drawSparkle(cx: Float, cy: Float, r: Float, color: Color) {
    repeat(4) { i ->
        rotate(degrees = i * 45f, pivot = Offset(cx, cy)) {
            val path = Path().apply {
                moveTo(cx, cy - r * 2.5f)
                lineTo(cx - r * 0.3f, cy)
                lineTo(cx, cy + r * 2.5f)
                lineTo(cx + r * 0.3f, cy)
                close()
            }
            drawPath(path, color = color)
        }
    }
}
