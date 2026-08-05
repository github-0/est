package com.example.evfunenhancer.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evfunenhancer.ui.strings.LocalAppStrings
import com.example.evfunenhancer.utils.countryFlag
import com.example.evfunenhancer.viewmodel.MainViewModel
import kotlin.random.Random
import kotlinx.coroutines.delay

private val StripGold   = Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFE06800)))
private val StripSilver = Brush.verticalGradient(listOf(Color(0xFFB8C0D0), Color(0xFF5A6270)))
private val StripBronze = Brush.verticalGradient(listOf(Color(0xFFD49860), Color(0xFF7A4A20)))

// Same highlight color used for the score flash on the Points screen.
private val FlashHighlight = Color(0xFF9666ff)

@Composable
private fun rememberFlashAlpha(value: Int, enabled: Boolean): Animatable<Float, AnimationVector1D> {
    val flashAlpha = remember { Animatable(0f) }
    var known by remember { mutableStateOf(value) }
    LaunchedEffect(value) {
        val shouldFlash = enabled && value != known
        known = value
        if (!shouldFlash) return@LaunchedEffect
        flashAlpha.snapTo(1f)
        flashAlpha.animateTo(0f, tween(1000))
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

@Composable
private fun DiagonalStrip(
    entry: SummaryEntry,
    gradient: Brush,
    onCard: Color,
    offsetDp: Dp,
    isFirst: Boolean,
    isLast: Boolean,
    isWinner: Boolean,
    translateCountry: (String) -> String,
    shimmerProgress: Float,
    flashEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val offsetPx      = with(LocalDensity.current) { offsetDp.toPx() }
    val outerOffsetPx = offsetPx / 3f
    val shape = remember(offsetPx, isFirst, isLast) {
        parallelogramShape(offsetPx = offsetPx, outerOffsetPx = outerOffsetPx, isFirst = isFirst, isLast = isLast)
    }
    val podiumMedals = medalString(entry.medals, PODIUM_MAX_MEDALS)
    val flagSize  = if (isWinner) 32.sp else 26.sp
    val nameSize  = if (isWinner) 9.sp  else 8.sp
    val scoreSize = if (isWinner) 28.sp else 22.sp
    val flashRadius = if (isWinner) 34.dp else 28.dp
    val flashAlpha = rememberFlashAlpha(entry.total, flashEnabled)

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush = gradient)
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
                                Color.White.copy(alpha = 0.4f),
                                Color.Transparent
                            ),
                            start = Offset(cx - sw, size.height / 2f),
                            end   = Offset(cx + sw, size.height / 2f)
                        )
                    )
                }
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.32f)),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = maxOf(size.width, size.height) * 0.7f
                    )
                )
            }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-8).dp)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text     = countryFlag(entry.country),
                fontSize = flagSize
            )
            Text(
                text       = translateCountry(entry.country).uppercase(),
                fontSize   = nameSize,
                color      = onCard,
                fontWeight = FontWeight.ExtraBold,
                textAlign  = TextAlign.Center
            )
            Text(
                text       = entry.total.toString(),
                fontSize   = scoreSize,
                color      = onCard,
                fontWeight = FontWeight.ExtraBold,
                modifier   = Modifier.drawBehind {
                    val fa = flashAlpha.value
                    if (fa > 0f) {
                        drawCircle(color = FlashHighlight.copy(alpha = fa * 0.6f), radius = flashRadius.toPx())
                    }
                }
            )
        }
        if (podiumMedals.isNotEmpty()) {
            Text(
                text      = podiumMedals,
                fontSize  = 11.sp,
                color     = onCard,
                textAlign = TextAlign.Center,
                modifier  = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
            )
        }
    }
}

private const val LIST_MAX_MEDALS = 10
private const val PODIUM_MAX_MEDALS = 3

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

private fun medalString(medals: Map<Int, Int>, maxVisible: Int): String {
    if (medals.isEmpty()) return ""
    val items = buildList {
        for (rank in 1..3) {
            val count = medals[rank] ?: continue
            val emoji = when (rank) { 1 -> "🥇"; 2 -> "🥈"; else -> "🥉" }
            repeat(count) { add(emoji) }
        }
    }
    if (items.isEmpty()) return ""
    return if (items.size <= maxVisible) items.joinToString("") else items.take(maxVisible).joinToString("") + "+"
}

@Composable
private fun DiagonalPodiumSection(
    top3: List<SummaryEntry>,
    translateCountry: (String) -> String,
    flashEnabled: Boolean
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

    val overlapDp = 12.dp
    val offsetDp  = 16.dp
    val density   = LocalDensity.current

    Layout(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(Color.Black),
        content = {
            DiagonalStrip(
                entry            = top3[1],
                gradient         = StripSilver,
                onCard           = Color.White,
                offsetDp         = offsetDp,
                isFirst          = true,
                isLast           = false,
                isWinner         = false,
                translateCountry = translateCountry,
                shimmerProgress  = shimmerAnimatables[0].value,
                flashEnabled     = flashEnabled,
                modifier         = Modifier.height(140.dp)
            )
            DiagonalStrip(
                entry            = top3[0],
                gradient         = StripGold,
                onCard           = MaterialTheme.colorScheme.onTertiary,
                offsetDp         = offsetDp,
                isFirst          = false,
                isLast           = false,
                isWinner         = true,
                translateCountry = translateCountry,
                shimmerProgress  = shimmerAnimatables[1].value,
                flashEnabled     = flashEnabled,
                modifier         = Modifier.height(140.dp)
            )
            DiagonalStrip(
                entry            = top3[2],
                gradient         = StripBronze,
                onCard           = Color.White,
                offsetDp         = offsetDp,
                isFirst          = false,
                isLast           = true,
                isWinner         = false,
                translateCountry = translateCountry,
                shimmerProgress  = shimmerAnimatables[2].value,
                flashEnabled     = flashEnabled,
                modifier         = Modifier.height(140.dp)
            )
        }
    ) { measurables, constraints ->
        val W         = constraints.maxWidth
        val H         = constraints.maxHeight
        val overlapPx = with(density) { overlapDp.roundToPx() }

        val w0 = W / 3
        val w1 = W / 3
        val w2 = W - w0 - w1

        val p0 = measurables[0].measure(
            constraints.copy(minWidth = w0,             maxWidth = w0,             minHeight = H, maxHeight = H)
        )
        val p1 = measurables[1].measure(
            constraints.copy(minWidth = w1 + overlapPx, maxWidth = w1 + overlapPx, minHeight = H, maxHeight = H)
        )
        val p2 = measurables[2].measure(
            constraints.copy(minWidth = w2 + overlapPx, maxWidth = w2 + overlapPx, minHeight = H, maxHeight = H)
        )

        layout(W, H) {
            p0.placeRelative(0,                   0)
            p1.placeRelative(w0 - overlapPx,      0)
            p2.placeRelative(w0 + w1 - overlapPx, 0)
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

    val participants = shows[selectedShowId] ?: emptyList()

    val ranked = participants
        .map { p ->
            val total = votes[p.order]?.values?.sum() ?: 0
            SummaryEntry(p.country, total, medalCounts[p.order] ?: emptyMap())
        }
        .sortedWith(compareByDescending<SummaryEntry> { it.total }.thenBy { it.country })

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
                    flashEnabled = totalsInitiallyLoaded
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
                    Text(
                        s.medalsHeader,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    )
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
            val flashAlpha = rememberFlashAlpha(entry.total, totalsInitiallyLoaded)
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
                val medals = medalString(entry.medals, LIST_MAX_MEDALS)
                if (medals.isNotEmpty()) {
                    Text(
                        medals,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
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
