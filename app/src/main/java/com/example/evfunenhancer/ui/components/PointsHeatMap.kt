package com.example.evfunenhancer.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

// Heat grid resolution in map units per cell (map space is 1000 × 913.5).
private const val CELL = 4
private val GRID_W = (EuropeMap.WIDTH / CELL).toInt()
private val GRID_H = ceil(EuropeMap.HEIGHT / CELL).toInt()

// Gaussian spread (σ) of one country's points, in map units (1 unit ≈ 3.8 km).
const val DEFAULT_HEAT_SPREAD = 55f
val HeatSpreadRange = 30f..110f

/** Spread in km, rounded to 10 km, for display. */
fun heatSpreadKm(spread: Float): Int = (spread * 3.8f / 10f).roundToInt() * 10

private val MapSea        = Color(0xFF12102A)
private val MapLand       = Color(0xFF211B3D)
private val MapCoastline  = Color(0xFFB8B0D8).copy(alpha = 0.22f)
private val MapInsetFrame = Color(0xFFB8B0D8).copy(alpha = 0.40f)
// Heat over the sea is drawn fainter than over land, but not hidden, so coastal
// and island entrants (Israel, Malta, Cyprus) keep their glow.
private const val SeaHeatAlpha = 0.45f

// App glow palette, cold → hot. Stops are spread evenly through the middle so
// mid-table countries don't all come out the same purple.
val HeatRampStops: List<Pair<Float, Color>> = listOf(
    0.00f to Color(0xFF28205A),
    0.15f to Color(0xFF6366F1),
    0.32f to Color(0xFFA855F7),
    0.50f to Color(0xFFEC4899),
    0.70f to Color(0xFFF97316),
    0.88f to Color(0xFFFFD700),
    1.00f to Color(0xFFFFF7D6),
)

private fun rampColor(t: Float): Color {
    val c = t.coerceIn(0f, 1f)
    for (i in 1 until HeatRampStops.size) {
        val (t1, c1) = HeatRampStops[i]
        if (c <= t1) {
            val (t0, c0) = HeatRampStops[i - 1]
            return lerp(c0, c1, (c - t0) / (t1 - t0))
        }
    }
    return HeatRampStops.last().second
}

// Heat value (0..255) → colour; low values fade out so cold areas show the land beneath.
private val heatLut: IntArray = IntArray(256) { i ->
    val t = i / 255f
    rampColor(t).copy(alpha = (t.pow(0.8f) * 1.15f).coerceAtMost(1f)).toArgb()
}

private val landPath: Path by lazy {
    PathParser().parsePathString(EuropeMap.landPath).toPath().apply { fillType = PathFillType.EvenOdd }
}

private val insetLandPath: Path by lazy {
    PathParser().parsePathString(EuropeMap.insetLandPath).toPath()
}

private val insetFramePath: Path by lazy {
    Path().apply { addRoundRect(RoundRect(EuropeMap.insetRect, CornerRadius(8f))) }
}

/**
 * Point density over the map, normalised to 0..1 against the top-scoring country's own total.
 */
class HeatField(val values: FloatArray)

private class Source(val x: Float, val y: Float, val points: Int)

/**
 * Heavy (~1.5 M exp calls); call off the main thread. Checks for cancellation per row so a
 * superseded calculation (e.g. while dragging the spread slider) stops early.
 * Returns null if no mapped country has points.
 */
suspend fun computeHeatField(pointsByCountry: Map<String, Int>, spread: Float = DEFAULT_HEAT_SPREAD): HeatField? {
    val sources = pointsByCountry.mapNotNull { (country, pts) ->
        val c = EuropeMap.centreOf(country) ?: return@mapNotNull null
        if (pts > 0) Source(c.x, c.y, pts) else null
    }
    if (sources.isEmpty()) return null
    val top = sources.maxBy { it.points }
    val (insetSources, mainSources) = sources.partition { EuropeMap.isInInset(it.x, it.y) }

    // Each country glows by its own points; where glows overlap the stronger one wins
    // (a smooth max, p = 4) instead of adding up. Summing would make clusters of small
    // neighbouring countries (the Balkans) look hotter than isolated high scorers.
    val twoS2 = 2f * spread * spread
    val values = FloatArray(GRID_W * GRID_H)
    for (gy in 0 until GRID_H) {
        coroutineContext.ensureActive()
        val y = gy * CELL + CELL / 2f
        for (gx in 0 until GRID_W) {
            val x = gx * CELL + CELL / 2f
            var sum4 = 0f
            for (s in if (EuropeMap.isInInset(x, y)) insetSources else mainSources) {
                val dx = x - s.x
                val dy = y - s.y
                val v = s.points * exp(-(dx * dx + dy * dy) / twoS2) / top.points
                val v2 = v * v
                sum4 += v2 * v2
            }
            values[gy * GRID_W + gx] = sqrt(sqrt(sum4)).coerceAtMost(1f)
        }
    }

    return HeatField(values)
}

private fun rasterise(values: FloatArray, progress: Float): ImageBitmap {
    val pixels = IntArray(values.size) { i -> heatLut[(values[i] * progress * 255f).toInt().coerceIn(0, 255)] }
    val bmp = Bitmap.createBitmap(GRID_W, GRID_H, Bitmap.Config.ARGB_8888)
    bmp.setPixels(pixels, 0, GRID_W, 0, 0, GRID_W, GRID_H)
    return bmp.asImageBitmap()
}

class HeatLayer(val field: HeatField?, val alpha: Float)

/**
 * Europe silhouette with one or more heat layers blended on top (used to crossfade between
 * datasets). [progress] scales heat intensity 0 → 1 for the entry animation.
 */
@Composable
fun PointsHeatMap(layers: List<HeatLayer>, progress: Float, modifier: Modifier = Modifier) {
    // Quantise so the entry animation re-rasterises ~30 times, not every frame.
    val step = (progress.coerceIn(0f, 1f) * 30f).roundToInt() / 30f
    val bitmaps = layers.map { layer ->
        val field = layer.field
        remember(field, step) { field?.let { rasterise(it.values, step) } }
    }
    val dstSize = IntSize(GRID_W * CELL, GRID_H * CELL)

    Canvas(
        modifier
            .aspectRatio(EuropeMap.WIDTH / EuropeMap.HEIGHT)
            .clip(RoundedCornerShape(10.dp))
    ) {
        drawRect(MapSea)
        val k = size.width / EuropeMap.WIDTH
        scale(k, k, pivot = Offset.Zero) {
            fun drawLandWithHeat(land: Path) {
                drawPath(land, MapLand)
                clipPath(land, clipOp = ClipOp.Difference) {
                    layers.forEachIndexed { i, layer ->
                        val heat = bitmaps[i] ?: return@forEachIndexed
                        if (layer.alpha > 0f) {
                            drawImage(heat, dstOffset = IntOffset.Zero, dstSize = dstSize, alpha = SeaHeatAlpha * layer.alpha, filterQuality = FilterQuality.High)
                        }
                    }
                }
                clipPath(land) {
                    layers.forEachIndexed { i, layer ->
                        val heat = bitmaps[i] ?: return@forEachIndexed
                        if (layer.alpha > 0f) {
                            drawImage(heat, dstOffset = IntOffset.Zero, dstSize = dstSize, alpha = layer.alpha, filterQuality = FilterQuality.High)
                        }
                    }
                }
                drawPath(land, MapCoastline, style = Stroke(width = 1f / k))
            }

            drawLandWithHeat(landPath)
            // Australia window, drawn over whatever of Europe lies beneath it.
            clipPath(insetFramePath) {
                drawPath(insetFramePath, MapSea)
                drawLandWithHeat(insetLandPath)
            }
            drawPath(insetFramePath, MapInsetFrame, style = Stroke(width = 1.5f / k))
        }
    }
}
