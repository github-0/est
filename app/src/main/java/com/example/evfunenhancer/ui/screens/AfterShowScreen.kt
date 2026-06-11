package com.example.evfunenhancer.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
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

private data class GuessScore(val username: String, val score: Int)
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
    val orderToParticipant: Map<Int, Participant>,
    val mostRobbed: List<RobScore>,
    val biggestSurprise: List<RobScore>,
    val userRanks: Map<Int, Int>,
    val officialRanks: Map<Int, Int>
)

private val CinzelBold = FontFamily(Font(R.font.cinzel_bold, FontWeight.Bold))

private val GlowPurple = Color(0xFFA855F7)
private val GlowPink   = Color(0xFFEC4899)
private val GlowGold   = Color(0xFFFFD700)
private val GlowSilver = Color(0xFFB8C0CC)
private val GlowBronze = Color(0xFFCD7F32)
private val GlowTeal   = Color(0xFF06B6D4)
private val GlowOrange = Color(0xFFF97316)

@Composable
fun AfterShowScreen(vm: MainViewModel) {
    val results by vm.results.collectAsState()
    val votes   by vm.votes.collectAsState()
    val guesses by vm.guesses.collectAsState()
    val shows   by vm.shows.collectAsState()
    val members by vm.members.collectAsState()

    fun resolveUid(uid: String) = members[uid] ?: uid.take(6)

    val r = results ?: return
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
        GuessScore(resolveUid(uid), score)
    }.filter { it.score > 0 }.sortedByDescending { it.score }

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
        orderToParticipant = orderToParticipant,
        mostRobbed         = mostRobbed,
        biggestSurprise    = biggestSurprise,
        userRanks          = userRanks,
        officialRanks      = officialRanks
    )

    val s = LocalAppStrings.current
    val context = LocalContext.current
    val graphicsLayer = rememberGraphicsLayer()
    var captureAction by remember { mutableStateOf<CaptureAction?>(null) }
    val listState = rememberLazyListState()
    val snappingLayout = remember(listState) { SnapLayoutInfoProvider(listState, SnapPosition.Center) }
    val snapBehavior = rememberSnapFlingBehavior(snappingLayout)

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            state = listState,
            flingBehavior = snapBehavior,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Column {
                    HeroHeader(data.year)
                    ShareSaveButtons(
                        onShare = { captureAction = CaptureAction.Share },
                        onSave  = { captureAction = CaptureAction.Save }
                    )
                }
            }
            item { GuessedWinnersCard(data) }
            item { SharedFeelingsCard(data) }
            item { MostGenerousCard(data) }
            item { MedalTableCard(data) }
            item { JudgedDifferentlyCard(data) }
            item { Spacer(Modifier.height(32.dp)) }
        }

        if (captureAction != null) {
            Column(
                modifier = Modifier
                    .alpha(0.002f)
                    .fillMaxWidth()
                    .wrapContentHeight(unbounded = true)
                    .drawWithContent {
                        graphicsLayer.record { this@drawWithContent.drawContent() }
                        drawLayer(graphicsLayer)
                    }
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

// ── Cards (used in both snapping LazyColumn and off-screen capture) ───────────

@Composable
private fun GuessedWinnersCard(data: AfterShowData) {
    val s = LocalAppStrings.current
    InfographicSection(s.aftershowGuessedWinners, GlowPurple) {
        if (!data.hasVotes) {
            Text(s.aftershowNoVotes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else if (data.guessScores.isEmpty()) {
            Text(s.aftershowNoGuesses, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            GuessLeaderboard(data.guessScores)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            s.aftershowScoringHint,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.28f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SharedFeelingsCard(data: AfterShowData) {
    val s = LocalAppStrings.current
    InfographicSection(s.aftershowSharedFeelings, GlowPink) {
        if (!data.hasVotes) {
            Text(s.aftershowNoVotes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SharedFeelPill("${data.groupOverlap10}/10", "TOP 10", GlowPurple, Modifier.weight(1f))
                SharedFeelPill("${data.groupOverlap5}/5",   "TOP 5",  GlowPink,   Modifier.weight(1f))
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
                    OverlapUserRow(uo)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun MostGenerousCard(data: AfterShowData) {
    val s = LocalAppStrings.current
    InfographicSection(s.aftershowMostGenerous, GlowTeal) {
        if (!data.hasVotes) {
            Text(s.aftershowNoVotes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            val maxPts = data.generousUsers.firstOrNull()?.second ?: 1
            val minPts = data.generousUsers.lastOrNull()?.second  ?: 0
            val range  = (maxPts - minPts).takeIf { it > 0 }?.toFloat() ?: 1f
            data.generousUsers.forEachIndexed { i, (user, pts) ->
                val fraction = 0.3f + 0.7f * (pts - minPts) / range
                GenerousVoterBar(i + 1, user, pts, fraction)
                if (i < data.generousUsers.lastIndex) Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun MedalTableCard(data: AfterShowData) {
    val s = LocalAppStrings.current
    InfographicSection(title = s.aftershowMedalTable, accentColor = GlowGold) {
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
                MedalRow(flag, name, cm, isTop3)
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
private fun JudgedDifferentlyCard(data: AfterShowData) {
    val s = LocalAppStrings.current
    InfographicSection(s.aftershowJudgedDifferently, GlowOrange) {
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
                    accentColor  = GlowOrange
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
                    accentColor  = GlowOrange
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
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        GuessedWinnersCard(data)
        SharedFeelingsCard(data)
        MostGenerousCard(data)
        MedalTableCard(data)
        JudgedDifferentlyCard(data)

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
private fun HeroHeader(year: Int) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF1E0A3E), Color(0xFF0D0B1E)))
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
    titleTrailing: (@Composable () -> Unit)? = null,
    titleBottomPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) = InfographicSection(icon = "", title = title, accentColor = accentColor, titleTrailing = titleTrailing, titleBottomPadding = titleBottomPadding, content = content)

@Composable
private fun InfographicSection(
    icon: String,
    title: String,
    accentColor: Color,
    titleTrailing: (@Composable () -> Unit)? = null,
    titleBottomPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
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

private val dotHeatGradient = listOf(
    Color(0xFF6366F1),
    Color(0xFFA855F7),
    Color(0xFFEC4899),
    Color(0xFFF43F5E),
    Color(0xFFF97316),
    Color(0xFFFFD700),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GuessLeaderboard(guessScores: List<GuessScore>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        guessScores.forEach { gs ->
            GuessChip(gs = gs)
        }
    }
}

@Composable
private fun GuessChip(gs: GuessScore) {
    Column(
        modifier = Modifier.padding(horizontal = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            gs.username,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(gs.score) { i ->
                val color = dotHeatGradient[i]
                Box(
                    Modifier
                        .size(11.dp)
                        .glow(color, radius = 6.dp, cornerRadius = 6.dp, alpha = 0.65f)
                        .background(color, CircleShape)
                )
            }
        }
    }
}

// ── Shared-feel pill ──────────────────────────────────────────────────────────

@Composable
private fun SharedFeelPill(stat: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
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
private fun OverlapUserRow(uo: UserOverlap) {
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
            OverlapBar(uo.top10 / 10f, "${uo.top10}/10", GlowPurple)
            OverlapBar(uo.top5  / 5f,  "${uo.top5}/5",   GlowPink)
        }
    }
}

@Composable
private fun OverlapBar(fraction: Float, label: String, color: Color) {
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
                        size = size.copy(width = size.width * fraction.coerceIn(0f, 1f)),
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
private fun GenerousVoterBar(rank: Int, user: String, pts: Int, fraction: Float) {
    val s = LocalAppStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .drawBehind {
                val barW = size.width * fraction
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
private fun MedalRow(flag: String, name: String, cm: CountryMedals, isTop3: Boolean) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
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

// ── Rank delta ────────────────────────────────────────────────────────────────

@Composable
private fun RankDeltaRow(flag: String, name: String, groupRank: Int, officialRank: Int, accentColor: Color = MaterialTheme.colorScheme.primary) {
    Row(
        Modifier
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
