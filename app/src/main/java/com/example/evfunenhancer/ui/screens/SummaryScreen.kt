package com.example.evfunenhancer.ui.screens

import android.os.SystemClock
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.Icon
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evfunenhancer.ui.strings.LocalAppStrings
import com.example.evfunenhancer.utils.countryFlag
import com.example.evfunenhancer.viewmodel.MainViewModel
import kotlin.random.Random
import kotlinx.coroutines.delay

// Metal tones per podium rank: highlight, base and shadow.
private class Metal(val hi: Color, val mid: Color, val lo: Color) {
    val text = Brush.verticalGradient(0.1f to hi, 0.55f to mid, 1f to lo)
    val edge = Brush.horizontalGradient(listOf(lo, hi, lo))
    val tint = Brush.verticalGradient(0f to mid.copy(alpha = 0.20f), 0.85f to mid.copy(alpha = 0.03f))
    // Lighter half only, so the small dots keep their hue.
    val dot  = Brush.verticalGradient(listOf(hi, mid))
}

// Gold is kept lemon-yellow and bronze reddish copper so the two don't blur together.
private val MetalGold   = Metal(Color(0xFFFFF4A3), Color(0xFFFFD60A), Color(0xFFB8900A))
private val MetalSilver = Metal(Color(0xFFF4F6FA), Color(0xFFB8C0D0), Color(0xFF646C7C))
private val MetalBronze = Metal(Color(0xFFF0A27A), Color(0xFFC0673A), Color(0xFF6E3014))

private const val PODIUM_HEIGHT_DP = 150
private val PodiumMetals = listOf(MetalGold, MetalSilver, MetalBronze)

// The guess medal dot for a podium rank (1–3), also used in the Points table.
internal fun medalDotBrush(rank: Int): Brush = PodiumMetals[rank - 1].dot

// Dark base under the strips' metal tint.
private val StripBase = Color(0xFF120F26)

// Same highlight color used for the score flash on the Points screen (rows 4+; the podium flashes in its metal colour).
private val FlashHighlight = Color(0xFF9666ff)

private const val FLASH_MS = 1000

// Uptime at which each country's total last changed. Keyed by country rather than by
// screen position, so when countries swap places only the one whose score changed flashes.
@Composable
private fun rememberFlashStamps(totals: Map<String, Int>, enabled: Boolean, resetKey: Any?): Map<String, Long> {
    val stamps = remember(resetKey) { mutableStateMapOf<String, Long>() }
    val known = remember(resetKey) { mutableMapOf<String, Int>() }
    LaunchedEffect(totals, resetKey) {
        val now = SystemClock.uptimeMillis()
        for ((country, total) in totals) {
            val previous = known.put(country, total)
            if (enabled && previous != null && previous != total) stamps[country] = now
        }
    }
    return stamps
}

// Flash for whichever country is shown here; a country that just moved here picks up
// its fade where it left off at its old position.
@Composable
private fun rememberFlashAlpha(country: String, stamp: Long?): Animatable<Float, AnimationVector1D> {
    val flashAlpha = remember(country) { Animatable(0f) }
    LaunchedEffect(country, stamp) {
        val elapsed = if (stamp == null) FLASH_MS else (SystemClock.uptimeMillis() - stamp).toInt()
        if (elapsed >= FLASH_MS) {
            flashAlpha.snapTo(0f)
            return@LaunchedEffect
        }
        flashAlpha.snapTo(1f - elapsed.toFloat() / FLASH_MS)
        flashAlpha.animateTo(0f, tween(FLASH_MS - elapsed))
    }
    return flashAlpha
}

private fun parallelogramShape(offsetPx: Float, outerOffsetPx: Float, isFirst: Boolean, isLast: Boolean): Shape =
    object : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val path = Path().apply {
                when {
                    isFirst -> {
                        moveTo(outerOffsetPx, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width - offsetPx, size.height)
                        lineTo(0f, size.height)
                    }
                    isLast -> {
                        moveTo(offsetPx, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width - outerOffsetPx, size.height)
                        lineTo(0f, size.height)
                    }
                    else -> {
                        moveTo(offsetPx, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width - offsetPx, size.height)
                        lineTo(0f, size.height)
                    }
                }
                close()
            }
            return Outline.Generic(path)
        }
    }

private val RankLineHeight = 20.sp
private val PodiumMoveSpec = spring<Float>(stiffness = Spring.StiffnessLow)

// Places the element at x within the podium, at the given width and the podium's full height.
private fun Modifier.podiumSlot(x: () -> Float, width: () -> Float) = layout { measurable, constraints ->
    val w = width().roundToInt()
    val placeable = measurable.measure(Constraints.fixed(w, constraints.maxHeight))
    layout(w, constraints.maxHeight) { placeable.place(x().roundToInt(), 0) }
}

private fun lerp(start: Float, stop: Float, fraction: Float) = start + (stop - start) * fraction

// The fixed part of a podium position: metal strip and rank number. The country shown on it
// is drawn separately by PodiumEntry, so it can slide between positions.
@Composable
private fun DiagonalStrip(
    rank: Int,
    metal: Metal,
    offsetDp: Dp,
    isFirst: Boolean,
    isLast: Boolean,
    shimmerProgress: Float,
    modifier: Modifier = Modifier
) {
    val offsetPx      = with(LocalDensity.current) { offsetDp.toPx() }
    val outerOffsetPx = offsetPx / 3f
    val shape = remember(offsetPx, isFirst, isLast) {
        parallelogramShape(offsetPx = offsetPx, outerOffsetPx = outerOffsetPx, isFirst = isFirst, isLast = isLast)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(StripBase)
            .background(brush = metal.tint)
            .drawWithContent {
                drawContent()
                if (shimmerProgress > 0f && shimmerProgress < 1f) {
                    val sw   = 30.dp.toPx()
                    val tilt = size.height * 0.3f
                    val cx   = -sw - tilt + shimmerProgress * (size.width + 2f * (sw + tilt))
                    val path = Path().apply {
                        moveTo(cx - sw + tilt, 0f)
                        lineTo(cx + sw + tilt, 0f)
                        lineTo(cx + sw - tilt, size.height)
                        lineTo(cx - sw - tilt, size.height)
                        close()
                    }
                    drawPath(
                        path  = path,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.28f),
                                Color.Transparent
                            ),
                            start = Offset(cx - sw, size.height / 2f),
                            end   = Offset(cx + sw, size.height / 2f)
                        )
                    )
                }
                drawRect(brush = metal.edge, size = Size(size.width, 2.dp.toPx()))
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            text       = "$rank",
            style      = TextStyle(brush = metal.text),
            fontSize   = 18.sp,
            lineHeight = RankLineHeight,
            fontWeight = FontWeight.ExtraBold,
            textAlign  = TextAlign.Center,
            modifier   = Modifier.padding(top = 10.dp)
        )
    }
}

// A country on the podium. Keyed by country by the caller, so when the order changes it
// slides from its old position to its new one and eases between the winner and runner-up sizes.
@Composable
private fun PodiumEntry(
    entry: SummaryEntry,
    rankIndex: Int,
    slotX: Float,
    slotWidth: Float,
    animateEntrance: Boolean,
    tied: Boolean,
    translateCountry: (String) -> String,
    flashStamp: Long?,
    showMedals: Boolean
) {
    val x        = remember { Animatable(slotX) }
    val width    = remember { Animatable(slotWidth) }
    val winner   = remember { Animatable(if (rankIndex == 0) 1f else 0f) }
    val entrance = remember { Animatable(if (animateEntrance) 0f else 1f) }
    LaunchedEffect(slotX) { x.animateTo(slotX, PodiumMoveSpec) }
    LaunchedEffect(slotWidth) { width.animateTo(slotWidth, PodiumMoveSpec) }
    LaunchedEffect(rankIndex) { winner.animateTo(if (rankIndex == 0) 1f else 0f, PodiumMoveSpec) }
    LaunchedEffect(Unit) { entrance.animateTo(1f, tween(450)) }

    val w = winner.value
    val flagSize    = lerp(24f, 30f, w).sp
    val scoreSize   = lerp(22f, 28f, w).sp
    val flashRadius = lerp(28f, 34f, w).dp
    val flashColor  = PodiumMetals[rankIndex].mid
    val flashAlpha  = rememberFlashAlpha(entry.country, flashStamp)
    val rankLineDp  = with(LocalDensity.current) { RankLineHeight.toDp() }

    Column(
        modifier = Modifier
            .podiumSlot(x = { x.value }, width = { width.value })
            .graphicsLayer {
                alpha = entrance.value
                translationY = (1f - entrance.value) * 24.dp.toPx()
            }
            .padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Same height as the strip's rank number; a tie link icon sits just to its right.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(rankLineDp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(20.dp))
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                // Qualified so the outer Row/Column scope overloads aren't picked up.
                androidx.compose.animation.AnimatedVisibility(
                    visible = tied,
                    enter   = fadeIn() + scaleIn(),
                    exit    = fadeOut() + scaleOut()
                ) {
                    Icon(
                        imageVector        = Icons.Rounded.Link,
                        contentDescription = null,
                        tint               = PodiumMetals[rankIndex].hi,
                        modifier           = Modifier.size(16.dp)
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text       = countryFlag(entry.country),
                    fontSize   = flagSize,
                    lineHeight = 1.15.em
                )
                Text(
                    text          = translateCountry(entry.country).uppercase(),
                    fontSize      = 8.5.sp,
                    letterSpacing = 0.16.em,
                    color         = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight    = FontWeight.Bold,
                    textAlign     = TextAlign.Center
                )
                Text(
                    text       = entry.total.toString(),
                    fontSize   = scoreSize,
                    lineHeight = 1.05.em,
                    color      = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.drawBehind {
                        val fa = flashAlpha.value
                        if (fa > 0f) {
                            drawCircle(color = flashColor.copy(alpha = fa * 0.6f), radius = flashRadius.toPx())
                        }
                    }
                )
            }
        }
        Box(
            modifier = Modifier.height(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (showMedals) MedalDots(entry.medals)
        }
    }
}

// Guess medals as a metal dot plus count per rank, e.g. ● 3 ● 1.
@Composable
private fun MedalDots(medals: Map<Int, Int>, modifier: Modifier = Modifier) {
    if (medals.isEmpty()) return
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (rank in 1..3) {
            val count = medals[rank] ?: continue
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(7.dp)
                        .background(medalDotBrush(rank), CircleShape)
                )
                Text(
                    text       = "$count",
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    style      = TextStyle(fontFeatureSettings = "tnum")
                )
            }
        }
    }
}

private data class SummaryEntry(
    val country: String,
    val total: Int,
    val medals: Map<Int, Int> = emptyMap()   // rank (1/2/3) → count
)

private fun buildMedalCounts(guesses: Map<String, Map<Int, Int>>): Map<Int, Map<Int, Int>> {
    val result = mutableMapOf<Int, MutableMap<Int, Int>>()
    for ((_, rankToOrder) in guesses) {
        for ((rank, order) in rankToOrder) {
            result.getOrPut(order) { mutableMapOf() }.merge(rank, 1, Int::plus)
        }
    }
    return result
}

@Composable
private fun DiagonalPodiumSection(
    top3: List<SummaryEntry>,
    translateCountry: (String) -> String,
    flashStamps: Map<String, Long>,
    tiedTotals: Set<Int>,
    showMedals: Boolean
) {
    val shimmerAnimatables = remember { List(3) { Animatable(0f) } }

    LaunchedEffect(Unit) {
        var lastPicked = -1
        while (true) {
            delay(Random.nextLong(2000, 6000))
            var i: Int
            do { i = Random.nextInt(3) } while (i == lastPicked)
            lastPicked = i
            shimmerAnimatables[i].snapTo(0f)
            shimmerAnimatables[i].animateTo(1f, animationSpec = tween(900, easing = LinearEasing))
        }
    }

    val overlapDp = 8.dp
    val offsetDp  = 16.dp
    val density   = LocalDensity.current

    // Countries already on the podium when it first appears don't play the entrance animation.
    var settled by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { settled = true }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(PODIUM_HEIGHT_DP.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        val W         = constraints.maxWidth
        val overlapPx = with(density) { overlapDp.roundToPx() }

        // Screen positions left to right hold ranks 2, 1, 3. The winner's strip is a little
        // wider than the other two; inner strips overlap their left neighbour.
        val w0 = (W / 3.12f).toInt()
        val w1 = (W * 1.12f / 3.12f).toInt()
        val w2 = W - w0 - w1
        val slotX     = listOf(0f, (w0 - overlapPx).toFloat(), (w0 + w1 - overlapPx).toFloat())
        val slotWidth = listOf(w0.toFloat(), (w1 + overlapPx).toFloat(), (w2 + overlapPx).toFloat())
        val rankToSlot = listOf(1, 0, 2)

        for (slot in 0..2) {
            val rankIndex = rankToSlot.indexOf(slot)
            DiagonalStrip(
                rank            = rankIndex + 1,
                metal           = PodiumMetals[rankIndex],
                offsetDp        = offsetDp,
                isFirst         = slot == 0,
                isLast          = slot == 2,
                shimmerProgress = shimmerAnimatables[slot].value,
                modifier        = Modifier.podiumSlot(x = { slotX[slot] }, width = { slotWidth[slot] })
            )
        }

        top3.forEachIndexed { rankIndex, entry ->
            key(entry.country) {
                val slot = rankToSlot[rankIndex]
                PodiumEntry(
                    entry            = entry,
                    rankIndex        = rankIndex,
                    slotX            = slotX[slot],
                    slotWidth        = slotWidth[slot],
                    animateEntrance  = settled,
                    tied             = entry.total in tiedTotals,
                    translateCountry = translateCountry,
                    flashStamp       = flashStamps[entry.country],
                    showMedals       = showMedals
                )
            }
        }
    }
}


@Composable
fun SummaryScreen(vm: MainViewModel = viewModel()) {
    val s = LocalAppStrings.current
    val shows by vm.shows.collectAsState()
    val selectedShowId by vm.selectedShowId.collectAsState()
    val votes by vm.votes.collectAsState()
    val guesses by vm.guesses.collectAsState()
    val medalCounts = remember(guesses) { buildMedalCounts(guesses) }

    // True only after the first non-empty votes snapshot has been rendered, so the
    // total-points flash is suppressed for the initial load (mirrors PointsScreen).
    var totalsInitiallyLoaded by remember { mutableStateOf(false) }
    SideEffect {
        if (!totalsInitiallyLoaded && votes.isNotEmpty()) totalsInitiallyLoaded = true
    }

    val showMedals = selectedShowId == "final"

    val participants = shows[selectedShowId] ?: emptyList()

    val ranked = participants
        .map { p ->
            val total = votes[p.order]?.values?.sum() ?: 0
            SummaryEntry(p.country, total, medalCounts[p.order] ?: emptyMap())
        }
        .sortedWith(compareByDescending<SummaryEntry> { it.total }.thenBy { it.country })

    val flashStamps = rememberFlashStamps(
        totals   = ranked.associate { it.country to it.total },
        enabled  = totalsInitiallyLoaded,
        resetKey = selectedShowId
    )

    // Point totals shared by two or more countries (0 points doesn't count as a tie).
    val tiedTotals = ranked
        .groupingBy { it.total }
        .eachCount()
        .filter { (total, count) -> total > 0 && count > 1 }
        .keys

    val showPodium = ranked.size >= 3

    val listState = rememberLazyListState()
    LaunchedEffect(ranked.firstOrNull()?.country) { listState.scrollToItem(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        state = listState
    ) {
        if (showPodium) {
            item(key = "podium") {
                DiagonalPodiumSection(
                    top3 = ranked.take(3),
                    translateCountry = s::translateCountry,
                    flashStamps = flashStamps,
                    tiedTotals = tiedTotals,
                    showMedals = showMedals
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }

        if (ranked.size > 3 || !showPodium) {
            item(key = "list_header") {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.width(28.dp))
                    Text(
                        s.countryHeader,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    )
                    if (showMedals) {
                        Text(
                            s.medalsHeader,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    Text(
                        s.totalPointsHeader,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.widthIn(min = 32.dp)
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }

        val listEntries = if (showPodium) ranked.drop(3) else ranked
        itemsIndexed(listEntries, key = { _, e -> e.country }) { index, entry ->
            val displayRank = if (showPodium) index + 4 else index + 1
            val rankColor = MaterialTheme.colorScheme.onSurfaceVariant
            val flashAlpha = rememberFlashAlpha(entry.country, flashStamps[entry.country])
            Row(
                Modifier
                    .fillMaxWidth()
                    .animateItem(placementSpec = spring(stiffness = Spring.StiffnessLow))
                    .padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$displayRank",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = rankColor,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(28.dp)
                )
                Text(
                    "${countryFlag(entry.country)} ${s.translateCountry(entry.country)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (showMedals) MedalDots(entry.medals, Modifier.padding(end = 8.dp))
                var totalTextLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
                Text(
                    entry.total.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    onTextLayout = { totalTextLayout = it },
                    modifier = Modifier
                        .widthIn(min = 32.dp)
                        .drawBehind {
                            val fa = flashAlpha.value
                            if (fa > 0f) {
                                val tl = totalTextLayout
                                val cx = if (tl != null) (tl.getLineLeft(0) + tl.getLineRight(0)) / 2f else size.width / 2f
                                drawCircle(
                                    color = FlashHighlight.copy(alpha = fa * 0.55f),
                                    radius = 17.dp.toPx(),
                                    center = Offset(cx, size.height / 2f)
                                )
                            }
                        }
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    }
}
