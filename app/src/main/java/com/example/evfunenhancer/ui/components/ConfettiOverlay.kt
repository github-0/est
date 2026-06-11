package com.example.evfunenhancer.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

private val CONFETTI_COLORS = listOf(
    Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF3F51B5),
    Color(0xFF03A9F4), Color(0xFF4CAF50), Color(0xFFFFEB3B),
    Color(0xFFFF5722), Color(0xFFFF9800)
)

private class ConfettiParticle(seed: Long) {
    private val rng = Random(seed)
    val startX = rng.nextFloat()
    val startY = -(rng.nextFloat() * 0.5f)
    val speedY = 0.5f + rng.nextFloat() * 0.6f
    val speedX = (rng.nextFloat() - 0.5f) * 0.2f
    val rotation = rng.nextFloat() * 360f
    val rotationSpeed = (rng.nextFloat() - 0.5f) * 720f
    val width = 13f + rng.nextFloat() * 15f
    val height = width * 0.4f
    val color = CONFETTI_COLORS[rng.nextInt(CONFETTI_COLORS.size)]
}

@Composable
fun ConfettiOverlay(onFinished: () -> Unit) {
    val particles = remember { List(45) { i -> ConfettiParticle(i.toLong()) } }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(1600, easing = LinearEasing))
        onFinished()
    }

    val p = progress.value

    Canvas(Modifier.fillMaxSize()) {
        val alpha = if (p > 0.75f) ((1f - p) / 0.25f).coerceIn(0f, 1f) else 1f
        particles.forEach { particle ->
            val cx = (particle.startX + particle.speedX * p) * size.width
            val cy = (particle.startY + particle.speedY * p) * size.height
            rotate(
                degrees = particle.rotation + particle.rotationSpeed * p,
                pivot = Offset(cx, cy)
            ) {
                drawRect(
                    color = particle.color.copy(alpha = alpha),
                    topLeft = Offset(cx - particle.width / 2, cy - particle.height / 2),
                    size = Size(particle.width, particle.height)
                )
            }
        }
    }
}
