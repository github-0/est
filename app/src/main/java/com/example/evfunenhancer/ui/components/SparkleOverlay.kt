package com.example.evfunenhancer.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val SPARKLE_COLORS = listOf(
    Color(0xFFFFD700), Color(0xFFFFFFFF), Color(0xFFEC4899), Color(0xFFA855F7)
)

private const val PARTICLE_COUNT = 10

@Composable
fun SparkleOverlay(modifier: Modifier = Modifier) {
    val rng = remember { Random(42) }
    val baseAlphas = remember { List(PARTICLE_COUNT) { Animatable(0f) } }
    val sparkleAlphas = remember { List(PARTICLE_COUNT) { Animatable(0f) } }
    // Anchor positions update while faded out so the particle re-centres elsewhere
    val anchors = remember { List(PARTICLE_COUNT) { mutableStateOf(Offset(rng.nextFloat(), rng.nextFloat())) } }
    val phaseX = remember { List(PARTICLE_COUNT) { rng.nextFloat() * 2f * PI.toFloat() } }
    val phaseY = remember { List(PARTICLE_COUNT) { rng.nextFloat() * 2f * PI.toFloat() } }
    val driftAmpX = remember { List(PARTICLE_COUNT) { rng.nextFloat() * 0.025f + 0.01f } }
    val driftAmpY = remember { List(PARTICLE_COUNT) { rng.nextFloat() * 0.025f + 0.01f } }
    val radii = remember { List(PARTICLE_COUNT) { rng.nextFloat() * 4f + 2f } }
    val colors = remember { List(PARTICLE_COUNT) { SPARKLE_COLORS[rng.nextInt(SPARKLE_COLORS.size)] } }

    val infiniteTransition = rememberInfiniteTransition(label = "sparkle-drift")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing)),
        label = "drift-time"
    )

    repeat(PARTICLE_COUNT) { i ->
        val baseAlpha = baseAlphas[i]
        val sparkleAlpha = sparkleAlphas[i]
        LaunchedEffect(i) {
            // Jump each particle to a random phase so the field is alive immediately
            anchors[i].value = Offset(Random.nextFloat() * 0.8f + 0.1f, Random.nextFloat() * 0.8f + 0.1f)
            when (Random.nextInt(4)) {
                1 -> { // mid-fade-in
                    val p = Random.nextFloat()
                    baseAlpha.snapTo(p)
                    baseAlpha.animateTo(1f, tween((1400 * (1f - p)).toInt().coerceAtLeast(1)))
                    kotlinx.coroutines.delay(Random.nextLong(500, 4000))
                    baseAlpha.animateTo(0f, tween(1400))
                    kotlinx.coroutines.delay(Random.nextLong(300, 1500))
                }
                2 -> { // fully visible
                    baseAlpha.snapTo(1f)
                    kotlinx.coroutines.delay(Random.nextLong(500, 4000))
                    baseAlpha.animateTo(0f, tween(1400))
                    kotlinx.coroutines.delay(Random.nextLong(300, 1500))
                }
                3 -> { // mid-fade-out
                    val p = Random.nextFloat()
                    baseAlpha.snapTo(1f - p)
                    baseAlpha.animateTo(0f, tween((1400 * (1f - p)).toInt().coerceAtLeast(1)))
                    kotlinx.coroutines.delay(Random.nextLong(300, 1500))
                }
                // 0: invisible, enters loop immediately
            }
            while (true) {
                anchors[i].value = Offset(
                    Random.nextFloat() * 0.8f + 0.1f,
                    Random.nextFloat() * 0.8f + 0.1f
                )
                baseAlpha.animateTo(1f, tween(1400))

                val visibleMs = Random.nextLong(2000, 5000)
                val sparkleAt = Random.nextLong(400, (visibleMs - 600).coerceAtLeast(401))
                kotlinx.coroutines.delay(sparkleAt)
                sparkleAlpha.animateTo(1f, tween(120))
                sparkleAlpha.animateTo(0f, tween(400))
                kotlinx.coroutines.delay(visibleMs - sparkleAt)

                baseAlpha.animateTo(0f, tween(1400))
                kotlinx.coroutines.delay(Random.nextLong(300, 1500))
            }
        }
    }

    Canvas(modifier = modifier) {
        repeat(PARTICLE_COUNT) { i ->
            val base = baseAlphas[i].value
            val sparkle = sparkleAlphas[i].value
            if (base < 0.01f && sparkle < 0.01f) return@repeat

            val anchor = anchors[i].value
            val cx = (anchor.x + sin(time + phaseX[i]) * driftAmpX[i]) * size.width
            val cy = (anchor.y + cos(time + phaseY[i]) * driftAmpY[i]) * size.height
            val color = colors[i]
            val radius = radii[i]

            drawCircle(
                color = color.copy(alpha = base * 0.3f + sparkle * 0.4f),
                radius = radius,
                center = Offset(cx, cy)
            )

            if (sparkle > 0.01f) {
                val rayLen = radius * 5f * sparkle
                val diagLen = rayLen * 0.55f
                drawLine(color.copy(alpha = sparkle), Offset(cx - rayLen, cy), Offset(cx + rayLen, cy), 1.5f)
                drawLine(color.copy(alpha = sparkle), Offset(cx, cy - rayLen), Offset(cx, cy + rayLen), 1.5f)
                drawLine(color.copy(alpha = sparkle * 0.6f), Offset(cx - diagLen, cy - diagLen), Offset(cx + diagLen, cy + diagLen), 1.5f)
                drawLine(color.copy(alpha = sparkle * 0.6f), Offset(cx - diagLen, cy + diagLen), Offset(cx + diagLen, cy - diagLen), 1.5f)
            }
        }
    }
}
