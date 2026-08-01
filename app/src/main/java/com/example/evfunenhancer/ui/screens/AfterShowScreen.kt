package com.example.evfunenhancer.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evfunenhancer.R
import com.example.evfunenhancer.data.CountryResult
import com.example.evfunenhancer.data.Participant
import com.example.evfunenhancer.ui.glow
import com.example.evfunenhancer.utils.countryFlag
import android.widget.Toast
import com.example.evfunenhancer.utils.saveBitmapToCache
import com.example.evfunenhancer.utils.saveToGallery
import com.example.evfunenhancer.utils.shareImage
import com.example.evfunenhancer.ui.strings.LocalAppStrings
import com.example.evfunenhancer.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class CaptureAction { Share, Save }

private data class GuessScore(val username: String, val score: Int, val picks: Map<Int, Int>)
private data class UserOverlap(val username: String, val top5: Int, val top10: Int)
private data class CountryMedals(val order: Int, val golds: Int, val silvers: Int, val bronzes: Int) {
    val total = golds + silvers + bronzes
    val weighted = 3 * golds + 2 * silvers + bronzes
}
private data class RobScore(val order: Int, val diff: Int)

private data class AfterShowData(
    val year: Int,
    val guessScores: List<GuessScore>,
    val hasVotes: Boolean,
    val groupOverlap5: Int,
    val groupOverlap10: Int,
    val userOverlaps: List<UserOverlap>,
    val generousUsers: List<Pair<String, Int>>,
    val countryMedals: List<CountryMedals>,
    val officialTop3Orders: Set<Int>,
    val officialTop3ByRank: Map<Int, Int>,
    val orderToParticipant: Map<Int, Participant>,
    val mostRobbed: List<RobScore>,
    val biggestSurprise: List<RobScore>,
    val userRanks: Map<Int, Int>,
    val officialRanks: Map<Int, Int>,
    val officialEntries: List<CountryResult>
)


private val CinzelBold = FontFamily(Font(R.font.cinzel_bold, FontWeight.Bold))

private val GlowPurple = Color(0xFFA855F7)
private val GlowPink   = Color(0xFFEC4899)
private val GlowGold   = Color(0xFFFFD700)
private val GlowSilver = Color(0xFFB8C0CC)
private val GlowBronze = Color(0xFFCD7F32)
private val GlowTeal   = Color(0xFF06B6D4)
private val GlowOrange = Color(0xFFF97316)
private val GlowGreen  = Color(0xFF4ADE80)
private val GlowIndigo = Color(0xFF6366F1)
private val GlowRose   = Color(0xFFF43F5E)

private val pageAccentColors = listOf(GlowPurple, GlowPink, GlowTeal, GlowGold, GlowOrange, GlowGreen)

private val guessRankColors = listOf(GlowGold, GlowPurple, GlowPink, GlowIndigo)
private fun guessRankColor(rank: Int): Color = guessRankColors[(rank - 1) % guessRankColors.size]

private enum class PickResult { EXACT, CLOSE, MISS }

private fun pickResult(pickRank: Int, pickedOrder: Int, officialTop3ByRank: Map<Int, Int>, officialTop3Orders: Set<Int>): PickResult =
    when {
        officialTop3ByRank[pickRank] == pickedOrder -> PickResult.EXACT
        pickedOrder in officialTop3Orders            -> PickResult.CLOSE
        else                                         -> PickResult.MISS
    }

@Composable
fun AfterShowScreen(vm: MainViewModel) {
    val results by vm.results.collectAsState()
    val votes   by vm.finalVotes.collectAsState()
    val guesses by vm.finalGuesses.collectAsState()
    val shows   by vm.shows.collectAsState()
    val members by vm.members.collectAsState()

    if (results == null) {
        AfterShowComingSoon()
        return
    }

    fun resolveUid(uid: String) = members[uid] ?: uid.take(6)

    val r = results!!
    val participants         = shows["final"] ?: emptyList()
    val orderToParticipant   = participants.associateBy { it.order }

    val voteSums: Map<Int, Int> = r.entries.associate { e ->
        e.order to (votes[e.order]?.values?.sum() ?: 0)
    }
    val hasVotes = voteSums.values.any { it > 0 }
    val userVoteSums: Map<String, Int> = buildMap {
        votes.forEach { (_, userPoints) ->
            userPoints.forEach { (uid, pts) ->
                val name = resolveUid(uid)
                put(name, (get(name) ?: 0) + pts)
            }
        }
    }
    val userRanks: Map<Int, Int> = if (hasVotes) voteSums.entries
        .sortedWith(
            compareByDescending<Map.Entry<Int, Int>> { it.value }
                .thenBy { orderToParticipant[it.key]?.country ?: "" }
        )
        .mapIndexed { i, e -> e.key to (i + 1) }
        .toMap() else emptyMap()
    val officialRanks: Map<Int, Int> = r.entries.associate { it.order to it.rank }
    val userVoteMap: Map<String, Map<Int, Int>> = run {
        val m = mutableMapOf<String, MutableMap<Int, Int>>()
        votes.forEach { (order, userPoints) ->
            userPoints.forEach { (uid, pts) -> m.getOrPut(resolveUid(uid)) { mutableMapOf() }[order] = pts }
        }
        m
    }

    val officialTop3ByRank: Map<Int, Int> = r.entries
        .filter { it.rank in 1..3 }
        .associate { it.rank to it.order }
    val officialTop3Orders: Set<Int> = officialTop3ByRank.values.toSet()
    val guessScores: List<GuessScore> = guesses.map { (uid, picks) ->
        var score = 0
        picks.forEach { (rank, order) ->
            if (order in officialTop3Orders) score += if (officialTop3ByRank[rank] == order) 2 else 1
        }
        GuessScore(resolveUid(uid), score, picks)
    }.filter { it.picks.isNotEmpty() }
     .sortedWith(
         compareBy<GuessScore> { if (it.score > 0) 0 else 1 }
             .thenByDescending { it.score }
             .thenBy { it.username }
     )

    val officialTop5:  Set<Int> = r.entries.sortedBy { it.rank }.take(5).map  { it.order }.toSet()
    val officialTop10: Set<Int> = r.entries.sortedBy { it.rank }.take(10).map { it.order }.toSet()
    val groupTop5:  Set<Int> = if (hasVotes) voteSums.entries.filter { it.value > 0 }.sortedByDescending { it.value }.take(5).map  { it.key }.toSet() else emptySet()
    val groupTop10: Set<Int> = if (hasVotes) voteSums.entries.filter { it.value > 0 }.sortedByDescending { it.value }.take(10).map { it.key }.toSet() else emptySet()
    val groupOverlap5  = groupTop5.intersect(officialTop5).size
    val groupOverlap10 = groupTop10.intersect(officialTop10).size
    val userOverlaps: List<UserOverlap> = if (hasVotes) userVoteMap.map { (user, orderPoints) ->
        val userTop5  = orderPoints.entries.filter { it.value > 0 }.sortedByDescending { it.value }.take(5).map  { it.key }.toSet()
        val userTop10 = orderPoints.entries.filter { it.value > 0 }.sortedByDescending { it.value }.take(10).map { it.key }.toSet()
        UserOverlap(user, userTop5.intersect(officialTop5).size, userTop10.intersect(officialTop10).size)
    }.sortedByDescending { it.top10 } else emptyList()

    val generousUsers = userVoteSums.entries.sortedByDescending { it.value }.map { it.key to it.value }

    val medalMap = mutableMapOf<Int, Triple<Int, Int, Int>>()
    guesses.values.forEach { picks ->
        picks.forEach { (rank, order) ->
            val (g, s, b) = medalMap.getOrDefault(order, Triple(0, 0, 0))
            medalMap[order] = when (rank) {
                1 -> Triple(g + 1, s, b)
                2 -> Triple(g, s + 1, b)
                3 -> Triple(g, s, b + 1)
                else -> Triple(g, s, b)
            }
        }
    }
    val countryMedals: List<CountryMedals> = medalMap
        .map { (order, t) -> CountryMedals(order, t.first, t.second, t.third) }
        .filter { it.total > 0 }
        .sortedWith(compareByDescending<CountryMedals> { it.total }.thenByDescending { it.weighted })

    val robScores = if (hasVotes) r.entries.filter { (voteSums[it.order] ?: 0) > 0 }.mapNotNull { e ->
        userRanks[e.order]?.let { ur -> RobScore(e.order, ur - e.rank) }
    } else emptyList()
    val mostRobbed      = robScores.sortedBy { it.diff }.take(3)
    val biggestSurprise = robScores.sortedByDescending { it.diff }.take(3)

    val data = AfterShowData(
        year               = r.year,
        guessScores        = guessScores,
        hasVotes           = hasVotes,
        groupOverlap5      = groupOverlap5,
        groupOverlap10     = groupOverlap10,
        userOverlaps       = userOverlaps,
        generousUsers      = generousUsers,
        countryMedals      = countryMedals,
        officialTop3Orders = officialTop3Orders,
        officialTop3ByRank = officialTop3ByRank,
        orderToParticipant = orderToParticipant,
        mostRobbed         = mostRobbed,
        biggestSurprise    = biggestSurprise,
        userRanks          = userRanks,
        officialRanks      = officialRanks,
        officialEntries    = r.entries
    )

    val s = LocalAppStrings.current
    val context = LocalContext.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val graphicsLayer = rememberGraphicsLayer()
    var captureAction by remember { mutableStateOf<CaptureAction?>(null) }
    val pagerState = rememberPagerState(pageCount = { 6 })
    var bgTime by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        var prev = 0L
        while (true) {
            withFrameNanos { t ->
                if (prev != 0L) bgTime += (t - prev) * 8.0e-10f
                prev = t
            }
        }
    }
    val bgTintColor by remember {
        derivedStateOf {
            val page   = pagerState.currentPage.coerceIn(0, pageAccentColors.lastIndex)
            val offset = pagerState.currentPageOffsetFraction
            val from   = pageAccentColors[page]
            val to     = when {
                offset > 0 -> pageAccentColors[(page + 1).coerceAtMost(pageAccentColors.lastIndex)]
                offset < 0 -> pageAccentColors[(page - 1).coerceAtLeast(0)]
                else       -> from
            }
            lerp(from, to, abs(offset).coerceIn(0f, 1f))
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                val page   = pagerState.currentPage.coerceIn(0, pageAccentColors.lastIndex)
                val offset = pagerState.currentPageOffsetFraction
                val from   = pageAccentColors[page]
                val to     = when {
                    offset > 0 -> pageAccentColors[(page + 1).coerceAtMost(pageAccentColors.lastIndex)]
                    offset < 0 -> pageAccentColors[(page - 1).coerceAtLeast(0)]
                    else       -> from
                }
                val tint = lerp(from, to, abs(offset).coerceIn(0f, 1f))

                val t = bgTime
                val twoPi = (2f * PI).toFloat()
                val rng = kotlin.random.Random(42L)
                repeat(80) {
                    val bx    = rng.nextFloat()
                    val by    = rng.nextFloat()
                    val r     = rng.nextFloat() * 3.2f + 1.2f
                    val baseA = rng.nextFloat() * 0.20f + 0.07f
                    val phX   = rng.nextFloat() * twoPi
                    val phY   = rng.nextFloat() * twoPi
                    val phA   = rng.nextFloat() * twoPi
                    val spX   = 0.38f + rng.nextFloat() * 0.34f
                    val spY   = 0.29f + rng.nextFloat() * 0.31f
                    val spA   = 0.55f + rng.nextFloat() * 0.60f
                    val dx    = rng.nextFloat() * 0.022f + 0.008f
                    val dy    = rng.nextFloat() * 0.022f + 0.008f
                    val x     = (bx + sin(t * spX + phX) * dx) * size.width
                    val y     = (by + cos(t * spY + phY) * dy) * size.height
                    val pulse = (1f + sin(t * spA + phA)) * 0.5f
                    val a     = (baseA * (0.35f + 0.65f * pulse)).coerceIn(0f, 1f)
                    drawCircle(color = tint.copy(alpha = a), radius = r, center = Offset(x, y))
                }
            }
    ) {
        Column(Modifier.fillMaxSize()) {
            HeroHeader(data.year, tintColor = bgTintColor)
            ShareSaveButtons(
                onShare = { captureAction = CaptureAction.Share },
                onSave  = { captureAction = CaptureAction.Save }
            )
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                pageSpacing = 8.dp,
                modifier = Modifier.weight(1f)
            ) { page ->
                var animVersion by remember { mutableIntStateOf(0) }
                LaunchedEffect(Unit) {
                    launch {
                        snapshotFlow { pagerState.currentPage }
                            .collect { current ->
                                if (current == page)
                                    animVersion += if (animVersion % 2 == 0) 1 else 2
                            }
                    }
                    snapshotFlow { pagerState.currentPage to pagerState.isScrollInProgress }
                        .collect { (current, scrolling) ->
                            if (current != page && !scrolling && animVersion % 2 == 1) animVersion++
                        }
                }
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.Center
                ) {
                    when (page) {
                        0 -> GuessedWinnersCard(data, animVersion = animVersion, sideInset = 0.dp)
                        1 -> SharedFeelingsCard(data, animVersion = animVersion, sideInset = 0.dp)
                        2 -> MostGenerousCard(data, animVersion = animVersion, sideInset = 0.dp)
                        3 -> MedalTableCard(data, animVersion = animVersion, sideInset = 0.dp)
                        4 -> JudgedDifferentlyCard(data, animVersion = animVersion, sideInset = 0.dp)
                        5 -> OfficialResultsCard(data, animVersion = animVersion, sideInset = 0.dp)
                    }
                    Spacer(Modifier.height(56.dp))
                }
            }
            PageIndicator(pagerState, pageCount = 6)
        }

        if (captureAction != null) {
            Column(
                modifier = Modifier
                    .alpha(0.002f)
                    .requiredWidth(screenWidth * 2)
                    .wrapContentHeight(unbounded = true)
                    .drawWithContent {
                        graphicsLayer.record { this@drawWithContent.drawContent() }
                        drawLayer(graphicsLayer)
                    }
                    .background(MaterialTheme.colorScheme.background)
            ) {
                HeroHeader(data.year)
                AfterShowContent(data = data)
            }
            LaunchedEffect(Unit) {
                withFrameNanos {}
                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                when (captureAction) {
                    CaptureAction.Share -> {
                        val uri = withContext(Dispatchers.IO) { saveBitmapToCache(context, bitmap) }
                        shareImage(context, uri)
                    }
                    CaptureAction.Save -> {
                        saveToGallery(context, bitmap)
                        Toast.makeText(context, s.aftershowSavedToPhotos, Toast.LENGTH_SHORT).show()
                    }
                    null -> {}
                }
                captureAction = null
            }
        }
    }
}

// ── Coming Soon screen (shown before official results are uploaded) ───────────

@Composable
private fun AfterShowComingSoon() {
    val s = LocalAppStrings.current
    var bgTime by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        var prev = 0L
        while (true) {
            withFrameNanos { t ->
                if (prev != 0L) bgTime += (t - prev) * 8.0e-10f
                prev = t
            }
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                val tint = GlowPurple
                val t = bgTime
                val twoPi = (2f * PI).toFloat()
                val rng = kotlin.random.Random(42L)
                repeat(80) {
                    val bx    = rng.nextFloat()
                    val by    = rng.nextFloat()
                    val r     = rng.nextFloat() * 3.2f + 1.2f
                    val baseA = rng.nextFloat() * 0.20f + 0.07f
                    val phX   = rng.nextFloat() * twoPi
                    val phY   = rng.nextFloat() * twoPi
                    val phA   = rng.nextFloat() * twoPi
                    val spX   = 0.38f + rng.nextFloat() * 0.34f
                    val spY   = 0.29f + rng.nextFloat() * 0.31f
                    val spA   = 0.55f + rng.nextFloat() * 0.60f
                    val dx    = rng.nextFloat() * 0.022f + 0.008f
                    val dy    = rng.nextFloat() * 0.022f + 0.008f
                    val x     = (bx + sin(t * spX + phX) * dx) * size.width
                    val y     = (by + cos(t * spY + phY) * dy) * size.height
                    val pulse = (1f + sin(t * spA + phA)) * 0.5f
                    val a     = (baseA * (0.35f + 0.65f * pulse)).coerceIn(0f, 1f)
                    drawCircle(color = tint.copy(alpha = a), radius = r, center = Offset(x, y))
                }
            }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ComingSoonHeroHeader()
            Spacer(Modifier.height(12.dp))
            Text(
                s.aftershowNotAvailableBody,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 36.dp)
            )
            Spacer(Modifier.height(4.dp))
            Column(Modifier.alpha(0.32f)) {
                PreviewCard(s.aftershowGuessedWinners, GlowPurple,  rows = 4)
                PreviewCard(s.aftershowSharedFeelings, GlowPink,    rows = 3)
                PreviewCard(s.aftershowOfficialResults, GlowGreen,  rows = 5)
            }
            Spacer(Modifier.height(56.dp))
        }
    }
}

@Composable
private fun ComingSoonHeroHeader() {
    val s = LocalAppStrings.current
    Box(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to Color(0xFF1E0A3E).copy(alpha = 0.5f),
                        1.0f to Color.Transparent
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "AFTERSHOW",
                style = TextStyle(fontFamily = CinzelBold, fontWeight = FontWeight.Bold, fontSize = 28.sp),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(3.dp))
            Text(
                s.aftershowComingSoon,
                style = MaterialTheme.typography.labelMedium,
                color = GlowPurple,
                letterSpacing = 5.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PreviewCard(title: String, accentColor: Color, rows: Int) {
    InfographicSection(title, accentColor) {
        val widths = listOf(0.85f, 0.70f, 0.55f, 0.75f, 0.60f)
        repeat(rows) { i ->
            Box(
                Modifier
                    .fillMaxWidth(widths[i % widths.size])
                    .height(13.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f))
            )
            if (i < rows - 1) Spacer(Modifier.height(10.dp))
        }
    }
}

// ── Share / Save buttons ──────────────────────────────────────────────────────

@Composable
private fun ShareSaveButtons(onShare: () -> Unit, onSave: () -> Unit) {
    val s = LocalAppStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
    ) {
        CaptureButton(
            label = s.aftershowShare,
            icon = Icons.Default.Share,
            onClick = onShare
        )
        CaptureButton(
            label = s.aftershowSave,
            icon = Icons.Default.Download,
            onClick = onSave
        )
    }
}

@Composable
private fun CaptureButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "buttonScale"
    )
    val borderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    val contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
    Row(
        modifier = Modifier
            .scale(scale)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .pointerInput(onClick) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        if (tryAwaitRelease()) {
                            onClick()
                            scope.launch {
                                delay(150)
                                isPressed = false
                            }
                        } else {
                            isPressed = false
                        }
                    }
                )
            }
            .padding(horizontal = 20.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(15.dp))
        Text(
            label,
            color = contentColor,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

// ── Page indicator ────────────────────────────────────────────────────────────

@Composable
private fun PageIndicator(pagerState: PagerState, pageCount: Int = 5) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { i ->
            val isSelected = pagerState.currentPage == i
            val width by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 8.dp,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "indicatorWidth_$i"
            )
            Box(
                Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isSelected) GlowPurple
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                    )
            )
            if (i < pageCount - 1) Spacer(Modifier.width(6.dp))
        }
    }
}

// ── Cards (used in both horizontal pager and off-screen capture) ──────────────

@Composable
private fun GuessedWinnersCard(data: AfterShowData, animVersion: Int = -1, sideInset: Dp = 16.dp) {
    val s = LocalAppStrings.current
    InfographicSection(s.aftershowGuessedWinners, GlowPurple, sideInset = sideInset) {
        if (!data.hasVotes) {
            Text(s.aftershowNoVotes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else if (data.guessScores.isEmpty()) {
            Text(s.aftershowNoGuesses, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            val scoredCount = data.guessScores.count { it.score > 0 }
            var scoredRank = 0
            data.guessScores.forEachIndexed { i, gs ->
                if (i > 0 && i != scoredCount) Spacer(Modifier.height(8.dp))
                if (i == scoredCount && scoredCount > 0) {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(16.dp))
                }
                val rank = if (gs.score > 0) { scoredRank++; scoredRank } else null
                GuessUserBlock(gs, rank, data, animVersion = animVersion, animDelayMs = (i * 70).toLong())
            }
        }
        if (data.officialTop3ByRank.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${s.aftershowOfficial}:",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.28f),
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.width(10.dp))
                listOf(1 to "🥇", 2 to "🥈", 3 to "🥉").forEachIndexed { idx, (rank, medal) ->
                    if (idx > 0) Spacer(Modifier.width(10.dp))
                    val order = data.officialTop3ByRank[rank]
                    val participant = order?.let { data.orderToParticipant[it] }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(medal, fontSize = 10.sp, modifier = Modifier.alpha(0.50f))
                        Text(
                            if (participant != null) countryFlag(participant.country) else "🏳️",
                            fontSize = 13.sp,
                            modifier = Modifier.alpha(0.58f)
                        )
                        Text(
                            if (participant != null) s.translateCountry(participant.country) else "",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f),
                            letterSpacing = 0.2.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SharedFeelingsCard(data: AfterShowData, animVersion: Int = -1, sideInset: Dp = 16.dp) {
    val s = LocalAppStrings.current
    InfographicSection(s.aftershowSharedFeelings, GlowPink, sideInset = sideInset) {
        if (!data.hasVotes) {
            Text(s.aftershowNoVotes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SharedFeelPill("${data.groupOverlap10}/10", "TOP 10", GlowPurple, Modifier.weight(1f), animVersion = animVersion, animDelayMs = 0L)
                SharedFeelPill("${data.groupOverlap5}/5",   "TOP 5",  GlowPink,   Modifier.weight(1f), animVersion = animVersion, animDelayMs = 120L)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                s.aftershowGroupAgreement,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.28f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            if (data.userOverlaps.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(12.dp))
                data.userOverlaps.forEach { uo ->
                    OverlapUserRow(uo, animVersion = animVersion)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun MostGenerousCard(data: AfterShowData, animVersion: Int = -1, sideInset: Dp = 16.dp) {
    val s = LocalAppStrings.current
    InfographicSection(s.aftershowMostGenerous, GlowTeal, sideInset = sideInset) {
        if (!data.hasVotes) {
            Text(s.aftershowNoVotes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            val maxPts = data.generousUsers.firstOrNull()?.second ?: 1
            val minPts = data.generousUsers.lastOrNull()?.second  ?: 0
            val range  = (maxPts - minPts).takeIf { it > 0 }?.toFloat() ?: 1f
            data.generousUsers.forEachIndexed { i, (user, pts) ->
                val fraction = 0.3f + 0.7f * (pts - minPts) / range
                GenerousVoterBar(i + 1, user, pts, fraction, animVersion = animVersion, animDelayMs = (i * 80).toLong())
                if (i < data.generousUsers.lastIndex) Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun MedalTableCard(data: AfterShowData, animVersion: Int = -1, sideInset: Dp = 16.dp) {
    val s = LocalAppStrings.current
    InfographicSection(title = s.aftershowMedalTable, accentColor = GlowGold, sideInset = sideInset) {
        Row(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
            Text(
                s.countryHeader,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp
            )
            Text("🥇", modifier = Modifier.width(34.dp), textAlign = TextAlign.Center, fontSize = 14.sp)
            Text("🥈", modifier = Modifier.width(34.dp), textAlign = TextAlign.Center, fontSize = 14.sp)
            Text("🥉", modifier = Modifier.width(34.dp), textAlign = TextAlign.Center, fontSize = 14.sp)
        }
        if (data.countryMedals.isEmpty()) {
            Text(s.aftershowNoMedals, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            data.countryMedals.forEachIndexed { i, cm ->
                val p      = data.orderToParticipant[cm.order]
                val isTop3 = cm.order in data.officialTop3Orders
                val flag   = if (p != null) countryFlag(p.country) else "🏳️"
                val name   = if (p != null) s.translateCountry(p.country) else s.aftershowCountryFallback(cm.order)
                MedalRow(flag, name, cm, isTop3, animVersion = animVersion, animDelayMs = (i * 70).toLong())
                if (i < data.countryMedals.lastIndex) {
                    Box(
                        Modifier.fillMaxWidth().height(0.5.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}

@Composable
private fun JudgedDifferentlyCard(data: AfterShowData, animVersion: Int = -1, sideInset: Dp = 16.dp) {
    val s = LocalAppStrings.current
    InfographicSection(s.aftershowJudgedDifferently, GlowOrange, sideInset = sideInset) {
        if (!data.hasVotes) {
            Text(s.aftershowNoVotes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Row(
                Modifier.fillMaxWidth().padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.width(2.dp).height(13.dp).background(MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(2.dp)))
                Spacer(Modifier.width(6.dp))
                Text(
                    s.aftershowMostRobbed,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(s.aftershowColGroup,    style = MaterialTheme.typography.labelSmall, color = GlowOrange.copy(alpha = 0.75f), fontWeight = FontWeight.Medium, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)
                    Text(s.aftershowColOfficial, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f), fontWeight = FontWeight.Medium, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)
                }
                Spacer(Modifier.width(12.dp))
            }
            Spacer(Modifier.height(2.dp))
            data.mostRobbed.forEachIndexed { i, rs ->
                val p = data.orderToParticipant[rs.order]
                RankDeltaRow(
                    flag         = if (p != null) countryFlag(p.country) else "🏳️",
                    name         = if (p != null) s.translateCountry(p.country) else s.aftershowCountryFallback(rs.order),
                    groupRank    = data.userRanks[rs.order] ?: 0,
                    officialRank = data.officialRanks[rs.order] ?: 0,
                    accentColor  = GlowOrange,
                    animVersion  = animVersion,
                    animDelayMs  = (i * 100).toLong()
                )
                if (i < data.mostRobbed.lastIndex) Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(16.dp))

            Row(
                Modifier.fillMaxWidth().padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.width(2.dp).height(13.dp).background(MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(2.dp)))
                Spacer(Modifier.width(6.dp))
                Text(
                    s.aftershowBiggestSurprise,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(s.aftershowColGroup,    style = MaterialTheme.typography.labelSmall, color = GlowOrange.copy(alpha = 0.75f), fontWeight = FontWeight.Medium, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)
                    Text(s.aftershowColOfficial, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f), fontWeight = FontWeight.Medium, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)
                }
                Spacer(Modifier.width(12.dp))
            }
            Spacer(Modifier.height(2.dp))
            data.biggestSurprise.forEachIndexed { i, rs ->
                val p = data.orderToParticipant[rs.order]
                RankDeltaRow(
                    flag         = if (p != null) countryFlag(p.country) else "🏳️",
                    name         = if (p != null) s.translateCountry(p.country) else s.aftershowCountryFallback(rs.order),
                    groupRank    = data.userRanks[rs.order] ?: 0,
                    officialRank = data.officialRanks[rs.order] ?: 0,
                    accentColor  = GlowOrange,
                    animVersion  = animVersion,
                    animDelayMs  = (i * 100).toLong()
                )
                if (i < data.biggestSurprise.lastIndex) Spacer(Modifier.height(6.dp))
            }
        }
    }
}

// ── All content (used for off-screen image capture) ───────────────────────────

@Composable
private fun AfterShowContent(data: AfterShowData, modifier: Modifier = Modifier, showFooter: Boolean = true) {
    val s = LocalAppStrings.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                GuessedWinnersCard(data)
                SharedFeelingsCard(data)
                MostGenerousCard(data)
            }
            Column(modifier = Modifier.weight(1f)) {
                MedalTableCard(data)
                JudgedDifferentlyCard(data)
                OfficialResultsCard(data)
            }
        }

        if (showFooter) {
            Spacer(Modifier.height(24.dp))
            Text(
                "Powered by",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            Image(
                painter = painterResource(R.drawable.est_banner_short),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(28.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

// ── Hero ─────────────────────────────────────────────────────────────────────

@Composable
private fun HeroHeader(year: Int, tintColor: Color = Color.Transparent) {
    val topColor = lerp(Color(0xFF1E0A3E), tintColor, 0.22f)
    Box(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f  to topColor.copy(alpha = 0.5f),
                        1.0f  to Color.Transparent
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "EUROVISION $year",
                style = TextStyle(fontFamily = CinzelBold, fontWeight = FontWeight.Bold, fontSize = 28.sp),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(3.dp))
            Text(
                "A F T E R S H O W",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFC4B0E8),
                letterSpacing = 6.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Section shell ─────────────────────────────────────────────────────────────

@Composable
private fun InfographicSection(
    title: String,
    accentColor: Color,
    sideInset: Dp = 16.dp,
    titleTrailing: (@Composable () -> Unit)? = null,
    titleBottomPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) = InfographicSection(icon = "", title = title, accentColor = accentColor, sideInset = sideInset, titleTrailing = titleTrailing, titleBottomPadding = titleBottomPadding, content = content)

@Composable
private fun InfographicSection(
    icon: String,
    title: String,
    accentColor: Color,
    sideInset: Dp = 16.dp,
    titleTrailing: (@Composable () -> Unit)? = null,
    titleBottomPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = sideInset)
            .padding(top = 16.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .border(1.dp, accentColor.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth().padding(start = 2.dp, bottom = titleBottomPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .width(3.dp)
                            .height(20.dp)
                            .background(accentColor, RoundedCornerShape(2.dp))
                    )
                    Spacer(Modifier.width(8.dp))
                    if (icon.isNotEmpty()) {
                        Text(icon, fontSize = 16.sp)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall,
                        color = accentColor,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    if (titleTrailing != null) {
                        Spacer(Modifier.weight(1f))
                        titleTrailing()
                    }
                }
                content()
            }
        }
    }
}

// ── Guess leaderboard ─────────────────────────────────────────────────────────

@Composable
private fun GuessUserBlock(
    gs: GuessScore,
    rankInLeaderboard: Int?,
    data: AfterShowData,
    animVersion: Int = -1,
    animDelayMs: Long = 0L
) {
    val isZero       = rankInLeaderboard == null
    val scoreColor   = if (!isZero) guessRankColor(rankInLeaderboard!!)
                       else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    val targetAlpha  = if (isZero) 0.40f else 1f
    val fillFraction = (gs.score / 6f).coerceIn(0f, 1f)
    val animFraction = remember(animVersion) { Animatable(if (animVersion == -1) fillFraction else 0f) }
    val offsetY      = remember(animVersion) { Animatable(if (animVersion == -1) 0f else 10f) }
    val blockAlpha   = remember(animVersion) { Animatable(if (animVersion == -1) targetAlpha else 0f) }
    LaunchedEffect(animVersion) {
        if (animVersion > 0 && animVersion % 2 == 1) {
            if (animDelayMs > 0L) delay(animDelayMs)
            launch { offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
            launch { animFraction.animateTo(fillFraction, tween(durationMillis = 600, easing = FastOutSlowInEasing)) }
            blockAlpha.animateTo(targetAlpha, tween(durationMillis = 220))
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = offsetY.value.dp)
            .alpha(blockAlpha.value)
            .border(1.dp, scoreColor.copy(alpha = 0.20f), RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .drawBehind { drawRect(color = scoreColor.copy(alpha = 0.11f), size = size.copy(width = size.width * animFraction.value)) }
            .padding(horizontal = 11.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Box(
            modifier = Modifier.size(22.dp).background(scoreColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (rankInLeaderboard != null) "$rankInLeaderboard" else "–",
                fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black.copy(alpha = 0.75f)
            )
        }
        Text(gs.username, fontSize = 14.sp, fontWeight = if (isZero) FontWeight.Normal else FontWeight.ExtraBold, modifier = Modifier.width(30.dp), maxLines = 1, overflow = TextOverflow.Clip)
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier.width(160.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                (1..3).forEach { pickRank ->
                    val order = gs.picks[pickRank]
                    if (order != null) {
                        val result      = pickResult(pickRank, order, data.officialTop3ByRank, data.officialTop3Orders)
                        val participant = data.orderToParticipant[order]
                        GuessPickChip(
                            flag      = if (participant != null) countryFlag(participant.country) else "🏳️",
                            result    = result,
                            pickRank  = pickRank
                        )
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            Text("${gs.score}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = scoreColor, lineHeight = 20.sp)
            Text("/6", fontSize = 10.sp, fontWeight = FontWeight.Normal, color = scoreColor.copy(alpha = 0.40f))
        }
    }
}

@Composable
private fun GuessPickChip(flag: String, result: PickResult, pickRank: Int) {
    val chipColor = when (result) {
        PickResult.EXACT -> GlowGreen
        PickResult.CLOSE -> Color(0xFFC084FC)
        PickResult.MISS  -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    }
    val ptsText     = when (result) { PickResult.EXACT -> "+2"; PickResult.CLOSE -> "+1"; PickResult.MISS -> "+0" }
    val medalColor  = when (pickRank) { 1 -> GlowGold; 2 -> GlowSilver; else -> GlowBronze }
    Row(
        modifier = Modifier
            .alpha(if (result == PickResult.MISS) 0.50f else 1f)
            .background(chipColor.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .border(1.dp, medalColor.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
            .padding(start = 6.dp, end = 8.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(flag, fontSize = 13.sp, lineHeight = 14.sp)
        Text(ptsText, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = chipColor)
    }
}


// ── Shared-feel pill ──────────────────────────────────────────────────────────

@Composable
private fun SharedFeelPill(stat: String, label: String, color: Color, modifier: Modifier = Modifier, animVersion: Int = -1, animDelayMs: Long = 0L) {
    val scale = remember(animVersion) { Animatable(if (animVersion == -1) 1f else 0.6f) }
    val alpha = remember(animVersion) { Animatable(if (animVersion == -1) 1f else 0f) }
    LaunchedEffect(animVersion) {
        if (animVersion > 0 && animVersion % 2 == 1) {
            if (animDelayMs > 0L) delay(animDelayMs)
            launch { scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)) }
            alpha.animateTo(1f, tween(250))
        }
    }
    Box(
        modifier = modifier
            .scale(scale.value)
            .alpha(alpha.value)
            .glow(color, radius = 14.dp, cornerRadius = 10.dp, alpha = 0.6f)
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .border(1.5.dp, color, RoundedCornerShape(10.dp))
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                stat,
                style = MaterialTheme.typography.titleLarge.copy(shadow = Shadow(color = color, offset = Offset.Zero, blurRadius = 20f), fontSize = 21.sp),
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

// ── Overlap rows ──────────────────────────────────────────────────────────────

@Composable
private fun OverlapUserRow(uo: UserOverlap, animVersion: Int = -1) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            uo.username,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(72.dp)
        )
        Column(
            Modifier.weight(1f).padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            OverlapBar(uo.top10 / 10f, "${uo.top10}/10", GlowPurple, animVersion = animVersion)
            OverlapBar(uo.top5  / 5f,  "${uo.top5}/5",   GlowPink,   animVersion = animVersion)
        }
    }
}

@Composable
private fun OverlapBar(fraction: Float, label: String, color: Color, animVersion: Int = -1) {
    val animFraction = remember(animVersion) { Animatable(if (animVersion == -1) fraction else 0f) }
    LaunchedEffect(animVersion) {
        if (animVersion > 0 && animVersion % 2 == 1) {
            animFraction.animateTo(fraction, tween(durationMillis = 600, easing = FastOutSlowInEasing))
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .weight(1f)
                .height(7.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(color.copy(alpha = 0.12f))
                .drawBehind {
                    drawRoundRect(
                        color = color.copy(alpha = 0.85f),
                        size = size.copy(width = size.width * animFraction.value.coerceIn(0f, 1f)),
                        cornerRadius = CornerRadius(99.dp.toPx())
                    )
                }
        )
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(30.dp),
            textAlign = TextAlign.End
        )
    }
}

// ── Generous voters ───────────────────────────────────────────────────────────

@Composable
private fun GenerousVoterBar(rank: Int, user: String, pts: Int, fraction: Float, animVersion: Int = -1, animDelayMs: Long = 0L) {
    val s = LocalAppStrings.current
    val animFraction = remember(animVersion) { Animatable(if (animVersion == -1) fraction else 0f) }
    LaunchedEffect(animVersion) {
        if (animVersion > 0 && animVersion % 2 == 1) {
            if (animDelayMs > 0L) delay(animDelayMs)
            animFraction.animateTo(fraction, tween(durationMillis = 600, easing = FastOutSlowInEasing))
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .drawBehind {
                val barW = size.width * animFraction.value
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(GlowTeal.copy(alpha = 0.28f), GlowTeal.copy(alpha = 0.10f)),
                        startX = 0f,
                        endX = barW.coerceAtLeast(1f)
                    ),
                    size = size.copy(width = barW),
                    cornerRadius = CornerRadius(8.dp.toPx())
                )
            }
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "$rank.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(24.dp)
            )
            Text(user, style = MaterialTheme.typography.bodyMedium, fontWeight = if (rank == 1) FontWeight.Bold else FontWeight.Normal)
        }
        Text(s.aftershowPts(pts), style = MaterialTheme.typography.labelMedium, color = GlowTeal, fontWeight = FontWeight.Bold)
    }
}

// ── Medal table ───────────────────────────────────────────────────────────────

@Composable
private fun MedalRow(flag: String, name: String, cm: CountryMedals, isTop3: Boolean, animVersion: Int = -1, animDelayMs: Long = 0L) {
    val offsetX = remember(animVersion) { Animatable(if (animVersion == -1) 0f else -14f) }
    val alpha   = remember(animVersion) { Animatable(if (animVersion == -1) 1f else 0f) }
    LaunchedEffect(animVersion) {
        if (animVersion > 0 && animVersion % 2 == 1) {
            if (animDelayMs > 0L) delay(animDelayMs)
            launch { offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
            alpha.animateTo(1f, tween(durationMillis = 250))
        }
    }
    Row(
        Modifier
            .offset(x = offsetX.value.dp)
            .alpha(alpha.value)
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(flag, fontSize = 20.sp)
        Spacer(Modifier.width(8.dp))
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(
                name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (isTop3) {
                Spacer(Modifier.width(4.dp))
                Text("★", style = MaterialTheme.typography.bodyMedium, color = GlowGold)
            }
        }
        MedalCountBadge(cm.golds,   GlowGold)
        MedalCountBadge(cm.silvers, GlowSilver)
        MedalCountBadge(cm.bronzes, GlowBronze)
    }
}

@Composable
private fun MedalCountBadge(count: Int, color: Color) {
    Box(Modifier.width(34.dp), contentAlignment = Alignment.Center) {
        if (count > 0) {
            Box(
                Modifier
                    .background(color.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
                    .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("$count", style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.ExtraBold)
            }
        } else {
            Text("—", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        }
    }
}

// ── Official results table ────────────────────────────────────────────────────

@Composable
private fun OfficialResultsCard(data: AfterShowData, animVersion: Int = -1, sideInset: Dp = 16.dp) {
    val s = LocalAppStrings.current
    InfographicSection(s.aftershowOfficialResults, GlowGreen, sideInset = sideInset) {
        Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "#",
                modifier = Modifier.width(24.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp
            )
            Spacer(Modifier.width(28.dp))
            Text(
                s.countryHeader,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp
            )
            Text(s.aftershowColJury,   modifier = Modifier.width(56.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.8.sp)
            Text(s.aftershowColPublic, modifier = Modifier.width(60.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.8.sp)
            Text(s.aftershowColTotal,  modifier = Modifier.width(58.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall, color = GlowGreen.copy(alpha = 0.85f), fontWeight = FontWeight.ExtraBold, letterSpacing = 0.8.sp)
        }
        val sorted = data.officialEntries.sortedBy { it.rank }
        sorted.forEachIndexed { i, entry ->
            val p = data.orderToParticipant[entry.order]
            OfficialResultRow(
                rank        = entry.rank,
                flag        = if (p != null) countryFlag(p.country) else "🏳️",
                name        = if (p != null) s.translateCountry(p.country) else s.aftershowCountryFallback(entry.order),
                juryScore   = entry.juryScore,
                publicScore = entry.publicScore,
                animVersion = animVersion,
                animDelayMs = (i * 30L).coerceAtMost(450L)
            )
            if (i < sorted.lastIndex) {
                Box(
                    Modifier.fillMaxWidth().height(0.5.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                )
            }
        }
    }
}

@Composable
private fun OfficialResultRow(
    rank: Int,
    flag: String,
    name: String,
    juryScore: Int,
    publicScore: Int,
    animVersion: Int = -1,
    animDelayMs: Long = 0L
) {
    val total = juryScore + publicScore
    val rankColor = when (rank) {
        1 -> GlowGold
        2 -> GlowSilver
        3 -> GlowBronze
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    }
    val offsetX = remember(animVersion) { Animatable(if (animVersion == -1) 0f else -14f) }
    val alpha   = remember(animVersion) { Animatable(if (animVersion == -1) 1f else 0f) }
    LaunchedEffect(animVersion) {
        if (animVersion > 0 && animVersion % 2 == 1) {
            if (animDelayMs > 0L) delay(animDelayMs)
            launch { offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
            alpha.animateTo(1f, tween(durationMillis = 220))
        }
    }
    Row(
        modifier = Modifier
            .offset(x = offsetX.value.dp)
            .alpha(alpha.value)
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$rank",
            modifier = Modifier.width(24.dp),
            style = MaterialTheme.typography.labelMedium,
            color = rankColor,
            fontWeight = if (rank <= 3) FontWeight.ExtraBold else FontWeight.Normal
        )
        Box(Modifier.width(28.dp)) {
            Text(flag, fontSize = 18.sp, lineHeight = 20.sp)
        }
        Text(
            name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (rank <= 3) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "$juryScore",
            modifier = Modifier.width(56.dp),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Text(
            "$publicScore",
            modifier = Modifier.width(60.dp),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Text(
            "$total",
            modifier = Modifier.width(58.dp),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.labelMedium,
            color = GlowGreen,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

// ── Rank delta ────────────────────────────────────────────────────────────────

@Composable
private fun RankDeltaRow(flag: String, name: String, groupRank: Int, officialRank: Int, accentColor: Color = MaterialTheme.colorScheme.primary, animVersion: Int = -1, animDelayMs: Long = 0L) {
    val offsetX = remember(animVersion) { Animatable(if (animVersion == -1) 0f else -14f) }
    val alpha = remember(animVersion) { Animatable(if (animVersion == -1) 1f else 0f) }
    LaunchedEffect(animVersion) {
        if (animVersion > 0 && animVersion % 2 == 1) {
            if (animDelayMs > 0L) delay(animDelayMs)
            launch { offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
            alpha.animateTo(1f, tween(250))
        }
    }
    Row(
        Modifier
            .offset(x = offsetX.value.dp)
            .alpha(alpha.value)
            .fillMaxWidth()
            .background(accentColor.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(flag, fontSize = 22.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.width(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            RankBadge("#$groupRank", accentColor)
            RankBadge("#$officialRank", MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RankBadge(value: String, color: Color) {
    Box(
        Modifier
            .width(50.dp)
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(6.dp))
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(value, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold)
    }
}
