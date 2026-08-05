package com.example.evfunenhancer.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evfunenhancer.ui.glow
import kotlinx.coroutines.launch

/** Icon glyphs for [TicketNavBar] tabs, hand-drawn to fit the ticket-stub concept. */
enum class TicketGlyph { PROFILE, VOTES, SUMMARY, AFTERSHOW }

data class TicketNavItem(
    val key: String,
    val label: String,
    val glyph: TicketGlyph,
    val stubBrush: Brush,
    val selected: Boolean,
    val enabled: Boolean,
    val showHighlight: Boolean = false,
    val onClick: () -> Unit,
)

/**
 * Bottom nav styled like a torn admission ticket: a punched perforation runs along the top
 * edge, and the active tab "stamps" onto a rotated colour block in that tab's own hue.
 */
@Composable
fun TicketNavBar(items: List<TicketNavItem>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
    ) {
        PerforationStrip()
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .height(72.dp)
        ) {
            val tabWidth = maxWidth / items.size
            val selectedIndex = items.indexOfFirst { it.selected }.coerceAtLeast(0)
            // Snaps instantly rather than animating: a triggered slide/bounce here is exactly
            // the kind of first-frame-after-cold-start animation that's prone to JIT warm-up
            // jank. The static tilt + stamp-coloured block still reads as a distinct concept
            // without depending on a smooth animation pipeline.
            val stubOffset = tabWidth * selectedIndex
            val stubInset = tabWidth * 0.09f
            Box(
                Modifier
                    .offset(x = stubOffset + stubInset, y = 8.dp)
                    .width(tabWidth - stubInset * 2)
                    .height(56.dp)
                    .graphicsLayer { rotationZ = -2f }
                    .clip(RoundedCornerShape(14.dp))
                    .background(items[selectedIndex].stubBrush)
            )
            Row(Modifier.fillMaxSize()) {
                items.forEach { item ->
                    TicketTab(item, Modifier.weight(1f).fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun PerforationStrip() {
    val holeColor = MaterialTheme.colorScheme.background
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(8.dp)
    ) {
        val spacing = 18.dp.toPx()
        val radius = 3.dp.toPx()
        var x = spacing / 2
        while (x < size.width) {
            drawCircle(color = holeColor, radius = radius, center = Offset(x, size.height / 2))
            x += spacing
        }
    }
}

private val StubInkColor = Color(0xFF1A0E2E)
private val UnlockAccentColor = Color(0xFF6D63FC)

@Composable
private fun TicketTab(item: TicketNavItem, modifier: Modifier = Modifier) {
    val dimAlpha by animateFloatAsState(
        targetValue = if (item.enabled) 0.5f else 0.25f,
        animationSpec = tween(400),
        label = "dimAlpha",
    )
    val dimColor = MaterialTheme.colorScheme.onBackground.copy(alpha = dimAlpha)
    val baseColor = if (item.selected) StubInkColor else dimColor

    // Only pay for the infinite shimmer transition on the tab that actually needs it,
    // instead of running one on every tab all the time.
    val contentColor = if (item.showHighlight && !item.selected) {
        val infiniteTransition = rememberInfiniteTransition(label = "aftershowShimmer")
        val shimmerT by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(2600), repeatMode = RepeatMode.Reverse),
            label = "shimmerT",
        )
        lerp(dimColor, Color(0xFFFFD700), shimmerT)
    } else baseColor

    // Pop + glow flash the moment a tab unlocks from its grayed-out state, so becoming
    // available reads as an event rather than just a silent color change.
    var wasEnabled by remember { mutableStateOf(item.enabled) }
    val unlockScale = remember { Animatable(1f) }
    val unlockGlow = remember { Animatable(0f) }
    LaunchedEffect(item.enabled) {
        if (item.enabled && !wasEnabled) {
            launch {
                unlockScale.snapTo(1f)
                unlockScale.animateTo(1.45f, tween(200, easing = FastOutSlowInEasing))
                unlockScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
            }
            launch {
                unlockGlow.snapTo(1f)
                unlockGlow.animateTo(0f, tween(900))
            }
        }
        wasEnabled = item.enabled
    }

    Box(
        modifier = modifier.clickable(
            enabled = item.enabled,
            onClick = item.onClick,
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
        ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.graphicsLayer {
                scaleX = unlockScale.value
                scaleY = unlockScale.value
            },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = if (unlockGlow.value > 0.01f)
                    Modifier.glow(
                        UnlockAccentColor,
                        radius = 12.dp,
                        cornerRadius = 100.dp,
                        alpha = unlockGlow.value * 0.6f,
                        style = BlurMaskFilter.Blur.NORMAL,
                    )
                else Modifier
            ) {
                TicketGlyphIcon(item.glyph, tint = contentColor, sizeDp = 22.dp)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = item.label.uppercase(),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.6.sp,
                color = baseColor,
            )
        }
    }
}

@Composable
private fun TicketGlyphIcon(glyph: TicketGlyph, tint: Color, sizeDp: Dp) {
    when (glyph) {
        TicketGlyph.PROFILE -> ProfileGlyph(tint, sizeDp)
        TicketGlyph.VOTES -> PointsGlyph(tint, sizeDp)
        TicketGlyph.SUMMARY -> BarsGlyph(tint, sizeDp)
        TicketGlyph.AFTERSHOW -> BurstGlyph(tint, sizeDp)
    }
}

@Composable
private fun ProfileGlyph(tint: Color, sizeDp: Dp) {
    Canvas(Modifier.size(sizeDp)) {
        val s = size.minDimension / 24f
        val stroke = Stroke(width = 2.1f * s, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawCircle(color = tint, radius = 3.5f * s, center = Offset(12f * s, 8f * s), style = stroke)
        val path = Path().apply {
            moveTo(4.5f * s, 19.5f * s)
            quadraticTo(12f * s, 9f * s, 19.5f * s, 19.5f * s)
        }
        drawPath(path, color = tint, style = stroke)
    }
}

@Composable
private fun PointsGlyph(tint: Color, sizeDp: Dp) {
    Box(Modifier.size(sizeDp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                color = tint,
                radius = size.minDimension / 2 - 1.5.dp.toPx(),
                style = Stroke(width = 1.8.dp.toPx()),
            )
        }
        Text(
            text = "12",
            fontSize = (sizeDp.value * 0.38f).sp,
            fontWeight = FontWeight.ExtraBold,
            color = tint,
        )
    }
}

@Composable
private fun BarsGlyph(tint: Color, sizeDp: Dp) {
    Canvas(Modifier.size(sizeDp)) {
        val s = size.minDimension / 24f
        val barWidth = 4.2f * s
        val corner = CornerRadius(1f * s, 1f * s)
        drawRoundRect(
            color = tint,
            topLeft = Offset(4.5f * s, 12f * s),
            size = Size(barWidth, 8f * s),
            cornerRadius = corner,
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(9.9f * s, 7f * s),
            size = Size(barWidth, 13f * s),
            cornerRadius = corner,
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(15.3f * s, 10f * s),
            size = Size(barWidth, 10f * s),
            cornerRadius = corner,
        )
    }
}

@Composable
private fun BurstGlyph(tint: Color, sizeDp: Dp) {
    Canvas(Modifier.size(sizeDp)) {
        val s = size.minDimension / 24f
        val strokeWidth = 2f * s
        val rays = listOf(
            Offset(12f, 2.6f) to Offset(12f, 6f),
            Offset(12f, 18f) to Offset(12f, 21.4f),
            Offset(21.4f, 12f) to Offset(18f, 12f),
            Offset(6f, 12f) to Offset(2.6f, 12f),
            Offset(18.5f, 5.5f) to Offset(16.1f, 7.9f),
            Offset(7.9f, 16.1f) to Offset(5.5f, 18.5f),
            Offset(18.5f, 18.5f) to Offset(16.1f, 16.1f),
            Offset(7.9f, 7.9f) to Offset(5.5f, 5.5f),
        )
        rays.forEach { (a, b) ->
            drawLine(
                color = tint,
                start = Offset(a.x * s, a.y * s),
                end = Offset(b.x * s, b.y * s),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
        drawCircle(color = tint, radius = 2.6f * s, center = Offset(12f * s, 12f * s))
    }
}
