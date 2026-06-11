package com.example.evfunenhancer.ui

import android.graphics.BlurMaskFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * OUTER blur: draws nothing inside the shape — only blurs outward from the edge.
 * Requires the composable's parent NOT to clip, otherwise the outer glow is invisible.
 *
 * [topInset] shifts the glow rect down inside the layout bounds. Use this for
 * OutlinedTextField wrappers: M3 reserves ~8 dp at the top for the floating label,
 * so without an inset the glow would appear above the visible outline border.
 */
fun Modifier.glow(
    color: Color,
    radius: Dp = 18.dp,
    cornerRadius: Dp = 100.dp,
    alpha: Float = 0.8f,
    topInset: Dp = 0.dp,
): Modifier = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint()
        val fp = paint.asFrameworkPaint()
        fp.isAntiAlias = true
        fp.color = color.copy(alpha = alpha).toArgb()
        fp.maskFilter = BlurMaskFilter(radius.toPx(), BlurMaskFilter.Blur.OUTER)
        val top = topInset.toPx()
        val r = cornerRadius.toPx().coerceAtMost((size.minDimension - top) / 2)
        canvas.drawRoundRect(0f, top, size.width, size.height, r, r, paint)
    }
}
