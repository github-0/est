package com.example.evfunenhancer.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PsychologyAlt
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import com.example.evfunenhancer.ui.strings.AppStrings
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.requiredSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
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
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import com.example.evfunenhancer.R
import com.example.evfunenhancer.data.CountryResult
import com.example.evfunenhancer.data.Participant
import com.example.evfunenhancer.ui.fontScaled
import com.example.evfunenhancer.ui.glow
import com.example.evfunenhancer.ui.components.DEFAULT_HEAT_SPREAD
import com.example.evfunenhancer.ui.components.EuropeMap
import com.example.evfunenhancer.ui.components.HeatSpreadRange
import com.example.evfunenhancer.ui.components.heatSpreadKm
import com.example.evfunenhancer.ui.components.HeatField
import com.example.evfunenhancer.ui.components.HeatLayer
import com.example.evfunenhancer.ui.components.HeatRampStops
import com.example.evfunenhancer.ui.components.PointsHeatMap
import com.example.evfunenhancer.ui.components.computeHeatField
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
private enum class CaptureFormat { AllCards, Summary }
private data class CaptureRequest(val action: CaptureAction, val format: CaptureFormat)

private data class GuessScore(val username: String, val score: Int, val picks: Map<Int, Int>)
private data class UserOverlap(val username: String, val top5: Int, val top10: Int)
private data class RobScore(val order: Int, val diff: Int)
private data class Compatibility(val username: String, val percent: Int)
// The points of everyone who scored one song, with their mean and standard deviation.
private data class SongSpread(val order: Int, val points: List<Int>, val mean: Double, val spread: Double)
private enum class AwardKind {
    Mainstream, Hipster, JuryBrain, TelevoteHeart, Nostradamus, SharpEye,
    Kingmaker, LostCause, HiveMind, LoneWolf, Chatterbox
}
// One member's award, with that member's own numbers: the similarity % (Mainstream, Hipster), the award's side and
// the other side (Jury Brain, Televote Heart), guess points (Nostradamus), hits in the official top 5 (Sharp Eye),
// the song's order of appearance and official rank (Kingmaker, Lost Cause), their average similarity to the others
// and the runner-up's (Hive Mind) or the group's average (Lone Wolf), or the number of comments (Chatterbox).
private data class Award(val kind: AwardKind, val uid: String, val username: String, val first: Int = 0, val second: Int = 0)

// Award thresholds, tuned against the 2026 final and simulated rooms (see dev notes): most voters get at
// least one award, and no award goes to nearly everyone.
private const val MainstreamMinMatch = 70
private const val HipsterMaxMatch = 45
private const val LeanMinGap = 10
private const val NostradamusMinScore = 2
private const val SharpEyeMinHits = 3
private const val LostCauseBottom = 5
// Two members' similarity over one final swings by about ±10 by chance, so these gaps keep chance winners rare.
private const val HiveMindMinLead = 4.0
private const val LoneWolfMinGap = 8.0
private const val PeerMinVoters = 3
private const val ChatterboxMinComments = 5

private data class AfterShowData(
    val year: Int,
    val guessScores: List<GuessScore>,
    val hasVotes: Boolean,
    val groupOverlap5: Int,
    val groupOverlap10: Int,
    val userOverlaps: List<UserOverlap>,
    val myUsername: String?,
    val myUid: String?,
    val hasOwnVotes: Boolean,
    val compatibilities: List<Compatibility>,
    val consensusVoterCount: Int,
    val consensusPicks: List<SongSpread>,
    val mostDivisive: List<SongSpread>,
    val awards: List<Award>,
    val generousUsers: List<Pair<String, Int>>,
    val officialTop3Orders: Set<Int>,
    val officialTop3ByRank: Map<Int, Int>,
    val orderToParticipant: Map<Int, Participant>,
    val mostRobbed: List<RobScore>,
    val biggestSurprise: List<RobScore>,
    val userRanks: Map<Int, Int>,
    val officialRanks: Map<Int, Int>,
    val officialEntries: List<CountryResult>,
    val roomHeat: HeatField?,
    val officialHeat: HeatField?,
    val offMapFlags: String
)


private val GlowPurple = Color(0xFFA855F7)
private val GlowPink   = Color(0xFFEC4899)
private val GlowGold   = Color(0xFFFFD700)
private val GlowSilver = Color(0xFFB8C0CC)
private val GlowTeal   = Color(0xFF06B6D4)
private val GlowOrange = Color(0xFFF97316)
private val GlowGreen  = Color(0xFF4ADE80)
private val GlowYellow = Color(0xFFFDE047)
private val GlowIndigo = Color(0xFF6366F1)
private val GlowRose   = Color(0xFFF43F5E)
private val GlowSky    = Color(0xFF38BDF8)
private val GlowFuchsia = Color(0xFFD946EF)

// The pager's cards in order: each one's jump strip glyph and the accent colour its page tints the background with.
private enum class AftershowCard(val icon: ImageVector, val accent: Color) {
    PointsMap(Icons.Default.Map, GlowIndigo),
    GuessedWinners(Icons.Default.PsychologyAlt, GlowPurple),
    MostGenerous(Icons.Default.Redeem, GlowTeal),
    SharedFeelings(Icons.Default.Favorite, GlowPink),
    Compatibility(Icons.Default.Group, GlowRose),
    Consensus(Icons.Default.Balance, GlowSky),
    RankShift(Icons.Default.SwapVert, GlowGold),
    Awards(Icons.Default.MilitaryTech, GlowFuchsia),
    OfficialResults(Icons.Default.Leaderboard, GlowGreen);

    fun title(s: AppStrings): String = when (this) {
        PointsMap         -> s.aftershowPointsMap
        GuessedWinners    -> s.aftershowGuessedWinners
        MostGenerous      -> s.aftershowMostGenerous
        SharedFeelings    -> s.aftershowSharedFeelings
        Compatibility     -> s.aftershowCompatibility
        Consensus         -> s.aftershowConsensus
        RankShift         -> s.aftershowRankShift
        Awards            -> s.aftershowAwards
        OfficialResults   -> s.aftershowOfficialResults
    }

    // Shorter name for the card jump strip, where long titles crowd the scrub bubble.
    fun stripLabel(s: AppStrings): String = when (this) {
        PointsMap         -> s.aftershowStripPointsMap
        GuessedWinners    -> s.aftershowStripGuessedWinners
        MostGenerous      -> s.aftershowStripMostGenerous
        OfficialResults   -> s.aftershowStripOfficialResults
        else              -> title(s)
    }
}


private val guessRankColors = listOf(GlowGold, GlowPurple, GlowPink, GlowIndigo)
private fun guessRankColor(rank: Int): Color = guessRankColors[(rank - 1) % guessRankColors.size]

// Warm-to-cool ramp: the most generous voter glows gold, the stingiest fades to indigo.
private val generousVoterColors = listOf(GlowIndigo, GlowPurple, GlowPink, GlowOrange, GlowGold)
private fun generousVoterColor(warmth: Float): Color {
    val t = warmth.coerceIn(0f, 1f) * generousVoterColors.lastIndex
    val i = t.toInt().coerceAtMost(generousVoterColors.lastIndex - 1)
    return lerp(generousVoterColors[i], generousVoterColors[i + 1], t - i)
}

private enum class PickResult { EXACT, CLOSE, MISS }

private fun pickResult(pickRank: Int, pickedOrder: Int, officialTop3ByRank: Map<Int, Int>, officialTop3Orders: Set<Int>): PickResult =
    when {
        officialTop3ByRank[pickRank] == pickedOrder -> PickResult.EXACT
        pickedOrder in officialTop3Orders            -> PickResult.CLOSE
        else                                         -> PickResult.MISS
    }

// Background tint behind a correct pick; misses are greyed out instead. Yellow goes olive over the
// dark card at a light tint, so the top-3 tint is stronger than the exact one.
private fun pickResultTint(result: PickResult): Color = when (result) {
    PickResult.EXACT -> GlowGreen.copy(alpha = 0.28f)
    PickResult.CLOSE -> GlowYellow.copy(alpha = 0.45f)
    PickResult.MISS  -> Color.Unspecified
}

private const val CompatibilityMinCommon = 3

// Taste match between two sets of points (two voters, or a voter and the official scores): Spearman rank
// correlation over the songs in both, mapped from -1..1 to 0..100 %. Null when too few songs overlap
// or either side gave them all the same points.
private fun matchPercent(mine: Map<Int, Int>, theirs: Map<Int, Int>): Int? {
    val common = mine.keys.filter { it in theirs }
    if (common.size < CompatibilityMinCommon) return null
    val a = averageRanks(common.map { mine.getValue(it) })
    val b = averageRanks(common.map { theirs.getValue(it) })
    val meanA = a.average()
    val meanB = b.average()
    var cov = 0.0; var varA = 0.0; var varB = 0.0
    for (i in a.indices) {
        val da = a[i] - meanA
        val db = b[i] - meanB
        cov += da * db; varA += da * da; varB += db * db
    }
    if (varA == 0.0 || varB == 0.0) return null
    val rho = cov / sqrt(varA * varB)
    return ((rho + 1) / 2 * 100).roundToInt().coerceIn(0, 100)
}

// Rank 1 = most points; tied values share the average of their ranks.
private fun averageRanks(values: List<Int>): List<Double> {
    val sorted = values.indices.sortedByDescending { values[it] }
    val ranks = DoubleArray(values.size)
    var i = 0
    while (i < sorted.size) {
        var j = i
        while (j + 1 < sorted.size && values[sorted[j + 1]] == values[sorted[i]]) j++
        val avg = (i + j) / 2.0 + 1
        for (k in i..j) ranks[sorted[k]] = avg
        i = j + 1
    }
    return ranks.toList()
}

@Composable
fun AfterShowScreen(vm: MainViewModel) {
    val results by vm.results.collectAsState()
    val votes   by vm.finalVotes.collectAsState()
    val guesses by vm.finalGuesses.collectAsState()
    val comments by vm.finalComments.collectAsState()
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
        .filter { it.value > 0 }
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
    fun guessScore(picks: Map<Int, Int>): Int {
        var score = 0
        picks.forEach { (rank, order) ->
            if (order in officialTop3Orders) score += if (officialTop3ByRank[rank] == order) 2 else 1
        }
        return score
    }
    val guessScores: List<GuessScore> = guesses.map { (uid, picks) ->
        GuessScore(resolveUid(uid), guessScore(picks), picks)
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

    // Keyed by UID (not name), so two members can't be merged by a shared username.
    val votesByUid: Map<String, Map<Int, Int>> = buildMap<String, MutableMap<Int, Int>> {
        votes.forEach { (order, userPoints) ->
            userPoints.forEach { (uid, pts) -> getOrPut(uid) { mutableMapOf() }[order] = pts }
        }
    }
    val myUid = vm.myUid
    val myVotes = myUid?.let { votesByUid[it] }.orEmpty()
    val compatibilities: List<Compatibility> = if (myVotes.isEmpty()) emptyList() else votesByUid
        .filterKeys { it != myUid }
        .mapNotNull { (uid, theirs) -> matchPercent(myVotes, theirs)?.let { Compatibility(resolveUid(uid), it) } }
        .sortedWith(compareByDescending<Compatibility> { it.percent }.thenBy { it.username })

    // Consensus / divisive: only the members who scored a song count towards it. A song needs at least
    // half of the final's voters (and at least 2) so one lone 12 can't be the consensus pick.
    val voterUids = votesByUid.filterValues { pts -> pts.values.any { it > 0 } }.keys
    val minScorers = maxOf(2, (voterUids.size + 1) / 2)
    val songSpreads: List<SongSpread> = if (voterUids.size < 2) emptyList() else r.entries.mapNotNull { e ->
        val pts = voterUids.mapNotNull { votesByUid.getValue(it)[e.order]?.takeIf { p -> p > 0 } }
        if (pts.size < minScorers) return@mapNotNull null
        val mean = pts.average()
        SongSpread(e.order, pts.sorted(), mean, sqrt(pts.sumOf { (it - mean) * (it - mean) } / pts.size))
    }
    val countryOf: (SongSpread) -> String = { orderToParticipant[it.order]?.country ?: "" }
    // Most agreed on: smallest spread, whatever the points; ties go to the song more members agreed on.
    val consensusPicks = songSpreads
        .sortedWith(compareBy<SongSpread> { it.spread }.thenByDescending { it.points.size }.thenByDescending { it.mean }.thenBy(countryOf))
        .take(3)
    // A song never appears in both lists, which could otherwise happen when only a few songs qualify.
    val mostDivisive = songSpreads.filter { it.spread > 0 && it !in consensusPicks }
        .sortedWith(compareByDescending<SongSpread> { it.spread }.thenByDescending { it.mean }.thenBy(countryOf))
        .take(3)

    // Awards: everyone who reaches an award's threshold gets it, except Hive Mind, Lone Wolf and Chatterbox, which go to
    // one member at most.
    val officialPoints = r.entries.associate { it.order to it.juryScore + it.publicScore }
    val juryPoints = r.entries.associate { it.order to it.juryScore }
    val televotePoints = r.entries.associate { it.order to it.publicScore }
    val winnerOrder = officialTop3ByRank[1]
    // Average agreement with the other voters; only meaningful with a few voters to compare against.
    val peerMatch: Map<String, Double> = if (voterUids.size < PeerMinVoters) emptyMap() else voterUids.mapNotNull { uid ->
        val mine = votesByUid.getValue(uid)
        val matches = voterUids.filter { it != uid }.mapNotNull { matchPercent(mine, votesByUid.getValue(it)) }
        if (matches.isEmpty()) null else uid to matches.average()
    }.toMap()
    val peerRanking = peerMatch.entries.sortedByDescending { it.value }.takeIf { it.size >= PeerMinVoters }.orEmpty()
    val awards: List<Award> = buildList {
        voterUids.forEach { uid ->
            val name = resolveUid(uid)
            val mine = votesByUid.getValue(uid).filterValues { it > 0 }
            fun award(kind: AwardKind, first: Int = 0, second: Int = 0) = add(Award(kind, uid, name, first, second))

            matchPercent(mine, officialPoints)?.let { official ->
                if (official >= MainstreamMinMatch) award(AwardKind.Mainstream, official)
                if (official <= HipsterMaxMatch) award(AwardKind.Hipster, official)
            }
            val jury = matchPercent(mine, juryPoints)
            val televote = matchPercent(mine, televotePoints)
            if (jury != null && televote != null) {
                if (jury - televote >= LeanMinGap) award(AwardKind.JuryBrain, jury, televote)
                if (televote - jury >= LeanMinGap) award(AwardKind.TelevoteHeart, televote, jury)
            }
            guesses[uid]?.let(::guessScore)?.takeIf { it >= NostradamusMinScore }?.let { award(AwardKind.Nostradamus, it) }
            // Your top 5 leaves out songs tied across 5th place, so a tie never counts in your favour.
            val myTop5 = mine.filter { (_, pts) -> mine.values.count { it >= pts } <= 5 }.keys
            (myTop5 intersect officialTop5).size.takeIf { it >= SharpEyeMinHits }?.let { award(AwardKind.SharpEye, it) }
            // Your favourites: every song you gave your highest points, whatever they were.
            val top = mine.values.maxOrNull()
            val favourites = mine.filterValues { it == top }.keys
            if (winnerOrder != null && winnerOrder in favourites) award(AwardKind.Kingmaker, winnerOrder, 1)
            favourites.filter { (officialRanks[it] ?: 0) > r.entries.size - LostCauseBottom }
                .maxByOrNull { officialRanks.getValue(it) }
                ?.let { flop -> award(AwardKind.LostCause, flop, officialRanks.getValue(flop)) }
        }
        // Hive Mind: the member clearly ahead of the runner-up in average similarity to the others. Lone Wolf: the member
        // clearly below the group's average (one outsider drags the average down, so Hive Mind measures against the runner-up).
        if (peerRanking.isNotEmpty()) {
            val (first, second) = peerRanking[0] to peerRanking[1]
            if (first.value - second.value >= HiveMindMinLead)
                add(Award(AwardKind.HiveMind, first.key, resolveUid(first.key), first.value.roundToInt(), second.value.roundToInt()))
            val last = peerRanking.last()
            val groupAverage = peerRanking.map { it.value }.average()
            if (groupAverage - last.value >= LoneWolfMinGap)
                add(Award(AwardKind.LoneWolf, last.key, resolveUid(last.key), last.value.roundToInt(), groupAverage.roundToInt()))
        }
        // Chatterbox goes to the one member, voter or not, who commented most on the final. A tie on the count goes to
        // whoever wrote more characters; if that ties too, nobody gets it.
        val commentStats = comments.values.flatMap { it.entries }.groupBy({ it.key }, { it.value.length })
            .mapValues { (_, lengths) -> lengths.size to lengths.sum() }
        val topCommenters = commentStats.entries
            .filter { it.value.first >= ChatterboxMinComments }
            .sortedWith(compareByDescending<Map.Entry<String, Pair<Int, Int>>> { it.value.first }.thenByDescending { it.value.second })
        topCommenters.firstOrNull()
            ?.takeIf { top -> topCommenters.getOrNull(1)?.value != top.value }
            ?.let { add(Award(AwardKind.Chatterbox, it.key, resolveUid(it.key), it.value.first)) }
    }.sortedWith(compareBy<Award> { it.kind }.thenBy { it.username })

    val robScores = if (hasVotes) r.entries.filter { (voteSums[it.order] ?: 0) > 0 }.mapNotNull { e ->
        userRanks[e.order]?.let { ur -> RobScore(e.order, ur - e.rank) }
    } else emptyList()
    val mostRobbed      = robScores.sortedBy { it.diff }.take(3)
    val biggestSurprise = robScores.sortedByDescending { it.diff }.take(3)

    val roomPointsByCountry: Map<String, Int> = voteSums.entries
        .mapNotNull { (order, pts) -> orderToParticipant[order]?.let { it.country to pts } }
        .toMap()
    val officialPointsByCountry: Map<String, Int> = r.entries
        .mapNotNull { e -> orderToParticipant[e.order]?.let { it.country to e.juryScore + e.publicScore } }
        .toMap()
    // Shared by the on-screen card's slider and the share image, so the saved image matches.
    var mapSpread by remember { mutableFloatStateOf(DEFAULT_HEAT_SPREAD) }
    val roomHeat by produceState<HeatField?>(null, roomPointsByCountry, mapSpread) {
        value = withContext(Dispatchers.Default) { computeHeatField(roomPointsByCountry, mapSpread) }
    }
    val officialHeat by produceState<HeatField?>(null, officialPointsByCountry, mapSpread) {
        value = withContext(Dispatchers.Default) { computeHeatField(officialPointsByCountry, mapSpread) }
    }
    val offMapFlags = participants
        .filter { EuropeMap.centreOf(it.country) == null }
        .joinToString(" ") { countryFlag(it.country) }

    val data = AfterShowData(
        year               = r.year,
        guessScores        = guessScores,
        hasVotes           = hasVotes,
        groupOverlap5      = groupOverlap5,
        groupOverlap10     = groupOverlap10,
        userOverlaps       = userOverlaps,
        myUsername         = myUid?.let { members[it] },
        myUid              = myUid,
        hasOwnVotes        = myVotes.isNotEmpty(),
        compatibilities    = compatibilities,
        consensusVoterCount = voterUids.size,
        consensusPicks     = consensusPicks,
        mostDivisive       = mostDivisive,
        awards             = awards,
        generousUsers      = generousUsers,
        officialTop3Orders = officialTop3Orders,
        officialTop3ByRank = officialTop3ByRank,
        orderToParticipant = orderToParticipant,
        mostRobbed         = mostRobbed,
        biggestSurprise    = biggestSurprise,
        userRanks          = userRanks,
        officialRanks      = officialRanks,
        officialEntries    = r.entries,
        roomHeat           = roomHeat,
        officialHeat       = officialHeat,
        offMapFlags        = offMapFlags
    )

    val s = LocalAppStrings.current
    val context = LocalContext.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val graphicsLayer = rememberGraphicsLayer()
    var captureRequest by remember { mutableStateOf<CaptureRequest?>(null) }
    // The Awards card is left out when nobody won anything.
    val cards = AftershowCard.entries.filter { it != AftershowCard.Awards || data.awards.isNotEmpty() }
    val pageAccentColors = cards.map { it.accent }
    val pageCount = cards.size
    // A multiple of every card count up to 10, so the first card stays put if the Awards card comes or goes.
    val startPage = (Int.MAX_VALUE / 2).let { it - it % 2520 }
    val pagerState = rememberPagerState(initialPage = startPage, pageCount = { Int.MAX_VALUE })
    // Story mode: one card at a time, Back/Next buttons, no peeking or page count — until gone through once.
    val scope = rememberCoroutineScope()
    val storyVersion by vm.aftershowStoryVersion.collectAsState()
    var storyMode by remember(r.year, storyVersion) { mutableStateOf(!vm.hasSeenAftershowStory(r.year)) }
    val storyLastPage = startPage + pageCount - 1
    // Survives tab switches like the pager's page, so a developer reset made elsewhere still restarts from the first card.
    var handledStoryVersion by rememberSaveable { mutableIntStateOf(storyVersion) }
    LaunchedEffect(storyMode, storyVersion) {
        if (storyMode && (storyVersion != handledStoryVersion || pagerState.currentPage !in startPage..storyLastPage))
            pagerState.scrollToPage(startPage)
        handledStoryVersion = storyVersion
    }
    fun storyStep(forward: Boolean) {
        val target = pagerState.targetPage + if (forward) 1 else -1
        if (target !in startPage..storyLastPage) return
        scope.launch {
            pagerState.animateScrollToPage(target, animationSpec = tween(380, easing = FastOutSlowInEasing))
            // Reaching the last card ends the story: the browse layout reveals around it.
            if (target == storyLastPage && storyMode) {
                storyMode = false
                vm.markAftershowStorySeen(r.year)
            }
        }
    }
    // The pager loops, but a jump moves in the strip's order: from the last card to the first scrolls back past all the
    // cards in between, not one step forward. Every jump takes the same time (the pager skips ahead to within a few pages of a far target before animating),
    // and only the target card plays its entry animation, not the ones passed on the way.
    var jumpTarget by remember { mutableStateOf<Int?>(null) }
    fun jumpToCard(card: Int) {
        val from = pagerState.targetPage
        val target = from + card - from.mod(pageCount)
        jumpTarget = target
        scope.launch {
            try {
                pagerState.animateScrollToPage(target, animationSpec = tween(380, easing = FastOutSlowInEasing))
            } finally {
                if (jumpTarget == target) jumpTarget = null
            }
        }
    }
    // 0 = story, 1 = browse. Card width stays constant: the page's own inset hands over to the pager's content padding.
    val reveal by animateFloatAsState(
        targetValue = if (storyMode) 0f else 1f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "storyReveal"
    )
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
    val bgTintColor by remember(pageAccentColors) {
        derivedStateOf {
            val page   = pagerState.currentPage.mod(pageCount)
            val offset = pagerState.currentPageOffsetFraction
            val from   = pageAccentColors[page]
            val to     = when {
                offset > 0 -> pageAccentColors[(page + 1).mod(pageCount)]
                offset < 0 -> pageAccentColors[(page - 1).mod(pageCount)]
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
                val page   = pagerState.currentPage.mod(pageCount)
                val offset = pagerState.currentPageOffsetFraction
                val from   = pageAccentColors[page]
                val to     = when {
                    offset > 0 -> pageAccentColors[(page + 1).mod(pageCount)]
                    offset < 0 -> pageAccentColors[(page - 1).mod(pageCount)]
                    else       -> from
                }
                val tint = lerp(from, to, abs(offset).coerceIn(0f, 1f))

                val t = bgTime
                val twoPi = (2f * PI).toFloat()
                // Parallax: the dots pan with the pager like a camera moving through space; bigger (nearer) dots move
                // further per card. Kept relative to startPage in Double, as the looping pager's page numbers are huge.
                val pagerPos = (pagerState.currentPage - startPage).toDouble() + offset
                val wrapMargin = 8.dp.toPx()
                val wrapWidth = size.width + 2 * wrapMargin
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
                    val depth = 0.08 + (r - 1.2f) / 3.2f * 0.30
                    val panX  = (bx - pagerPos * depth).mod(1.0).toFloat() * wrapWidth - wrapMargin
                    val x     = panX + sin(t * spX + phX) * dx * size.width
                    val y     = (by + cos(t * spY + phY) * dy) * size.height
                    val pulse = (1f + sin(t * spA + phA)) * 0.5f
                    val a     = (baseA * (0.35f + 0.65f * pulse)).coerceIn(0f, 1f)
                    drawCircle(color = tint.copy(alpha = a), radius = r, center = Offset(x, y))
                }
            }
    ) {
        Column(Modifier.fillMaxSize()) {
            HeroHeader(data.year, tintColor = bgTintColor)
            AnimatedVisibility(
                visible = !storyMode,
                enter = expandVertically(tween(500)) + fadeIn(tween(500, delayMillis = 200))
            ) {
                ShareSaveButtons(
                    loadingAction = captureRequest?.action,
                    onRequest = { if (captureRequest == null) captureRequest = it }
                )
            }
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 16.dp * reveal),
                pageSpacing = 8.dp,
                userScrollEnabled = !storyMode,
                modifier = Modifier.weight(1f)
            ) { page ->
                var animVersion by remember { mutableIntStateOf(0) }
                LaunchedEffect(Unit) {
                    launch {
                        snapshotFlow { pagerState.currentPage }
                            .collect { current ->
                                if (current == page && (jumpTarget == null || jumpTarget == page))
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
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp * (1f - reveal)),
                    verticalArrangement = Arrangement.Center
                ) {
                    when (cards[page.mod(pageCount)]) {
                        AftershowCard.PointsMap -> PointsMapCard(
                            data, animVersion = animVersion, sideInset = 0.dp,
                            spread = mapSpread, onSpreadChange = { mapSpread = it }
                        )
                        AftershowCard.GuessedWinners    -> GuessedWinnersCard(data, animVersion = animVersion, sideInset = 0.dp)
                        AftershowCard.MostGenerous      -> MostGenerousCard(data, animVersion = animVersion, sideInset = 0.dp)
                        AftershowCard.SharedFeelings    -> SharedFeelingsCard(data, animVersion = animVersion, sideInset = 0.dp)
                        AftershowCard.Compatibility     -> CompatibilityCard(data, animVersion = animVersion, sideInset = 0.dp, showInfo = true)
                        AftershowCard.Consensus         -> GroupConsensusCard(data, animVersion = animVersion, sideInset = 0.dp, showInfo = true)
                        AftershowCard.RankShift         -> RankShiftCard(data, animVersion = animVersion, sideInset = 0.dp)
                        AftershowCard.Awards            -> AwardsCard(data, animVersion = animVersion, sideInset = 0.dp, showInfo = true, collapsible = true)
                        AftershowCard.OfficialResults   -> OfficialResultsCard(data, animVersion = animVersion, sideInset = 0.dp)
                    }
                    Spacer(Modifier.height(56.dp))
                }
            }
            // One slot for the story buttons and the card jump strip; it shrinks to the strip's height on reveal.
            Box(
                Modifier.fillMaxWidth().height(lerp(60.dp, 56.dp, reveal)),
                contentAlignment = Alignment.Center
            ) {
                if (reveal > 0f) Box(Modifier.alpha(reveal)) {
                    CardJumpStrip(
                        cards = cards,
                        // A jump's target is active at once, not only once the pager has scrolled there.
                        activeCard = (jumpTarget ?: pagerState.currentPage).mod(pageCount),
                        enabled = !storyMode,
                        onJump = ::jumpToCard
                    )
                }
                if (reveal < 1f) Row(
                    Modifier.alpha(1f - reveal),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CaptureButton(
                        label = s.aftershowStoryBack,
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        isLoading = false,
                        enabled = storyMode && pagerState.targetPage > startPage,
                        onClick = { storyStep(forward = false) }
                    )
                    CaptureButton(
                        label = s.aftershowStoryNext,
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        isLoading = false,
                        enabled = storyMode,
                        iconTrailing = true,
                        onClick = { storyStep(forward = true) }
                    )
                }
            }
        }

        val request = captureRequest
        if (request != null) {
            val recordModifier = Modifier.drawWithContent {
                graphicsLayer.record { this@drawWithContent.drawContent() }
                drawLayer(graphicsLayer)
            }
            when (request.format) {
                CaptureFormat.AllCards -> Column(
                    modifier = Modifier
                        .alpha(0.002f)
                        .requiredWidth(screenWidth * 2)
                        .wrapContentHeight(unbounded = true)
                        .then(recordModifier)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    AfterShowContent(data = data)
                }
                // Fixed density and font scale, so the card is always 1080 × 1920 px and lays out the same on every device.
                CaptureFormat.Summary -> Box(Modifier.alpha(0.002f)) {
                    CompositionLocalProvider(LocalDensity provides Density(SummaryCardPxPerDp, fontScale = 1f)) {
                        SummaryCard(data, Modifier.requiredSize(SummaryCardWidth, SummaryCardHeight).then(recordModifier))
                    }
                }
            }
            LaunchedEffect(request) {
                withFrameNanos {}
                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                try {
                    when (request.action) {
                        CaptureAction.Share -> {
                            val uri = withContext(Dispatchers.IO) { saveBitmapToCache(context, bitmap) }
                            shareImage(context, uri)
                        }
                        CaptureAction.Save -> {
                            saveToGallery(context, bitmap)
                            Toast.makeText(context, s.aftershowSavedToPhotos, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (_: Exception) {
                    Toast.makeText(context, s.aftershowSaveShareFailed, Toast.LENGTH_SHORT).show()
                }
                captureRequest = null
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
        AftershowLockup(s.aftershowComingSoon)
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
private fun ShareSaveButtons(loadingAction: CaptureAction?, onRequest: (CaptureRequest) -> Unit) {
    val s = LocalAppStrings.current
    // Each button opens a menu to pick the image format first.
    var menuFor by remember { mutableStateOf<CaptureAction?>(null) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
    ) {
        listOf(
            Triple(CaptureAction.Share, s.aftershowShare, Icons.Default.Share),
            Triple(CaptureAction.Save, s.aftershowSave, Icons.Default.Download)
        ).forEach { (action, label, icon) ->
            Box {
                CaptureButton(
                    label = label,
                    icon = icon,
                    isLoading = loadingAction == action,
                    enabled = loadingAction == null,
                    onClick = { menuFor = action }
                )
                DropdownMenu(expanded = menuFor == action, onDismissRequest = { menuFor = null }) {
                    listOf(
                        Triple(CaptureFormat.AllCards, s.aftershowFormatAllCards, Icons.Default.ViewAgenda),
                        Triple(CaptureFormat.Summary, s.aftershowFormatSummary, Icons.Default.CropPortrait)
                    ).forEach { (format, formatLabel, formatIcon) ->
                        DropdownMenuItem(
                            text = { Text(formatLabel) },
                            leadingIcon = { Icon(formatIcon, contentDescription = null) },
                            onClick = {
                                menuFor = null
                                onRequest(CaptureRequest(action, format))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CaptureButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isLoading: Boolean,
    enabled: Boolean,
    iconTrailing: Boolean = false,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "buttonScale"
    )
    val borderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 0.35f else 0.18f)
    val contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 0.65f else 0.35f)
    Row(
        modifier = Modifier
            .scale(scale)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .then(
                if (enabled) {
                    Modifier.pointerInput(onClick) {
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
                } else Modifier
            )
            .padding(horizontal = 20.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!iconTrailing) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(15.dp),
                    color = contentColor,
                    strokeWidth = 1.5.dp
                )
            } else {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(15.dp))
            }
        }
        Text(
            label,
            color = contentColor,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.labelMedium
        )
        if (iconTrailing) Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(15.dp))
    }
}

// ── Card jump strip ───────────────────────────────────────────────────────────

// One glyph per card in its accent colour; the active card's sits in a glowing circle. Tap a glyph to jump to its card,
// or press and drag along the strip to scrub through the cards. While pressed, a bubble names the card under the finger.
@Composable
private fun CardJumpStrip(cards: List<AftershowCard>, activeCard: Int, enabled: Boolean, onJump: (card: Int) -> Unit) {
    val s = LocalAppStrings.current
    val jump by rememberUpdatedState(onJump)
    var pressedCard by remember { mutableStateOf<Int?>(null) }
    BoxWithConstraints(Modifier.fillMaxWidth().padding(vertical = 6.dp).height(44.dp)) {
        // The slots share the row's width (inside the pager's 16 dp gutters); glyphs fill their slots up to the strip's height.
        val slot = (maxWidth - 32.dp) / cards.size
        val stripStart = 16.dp
        val glyphSize = minOf(slot, 40.dp)
        val slotPx = with(LocalDensity.current) { slot.toPx() }
        Row(
            Modifier
                .align(Alignment.Center)
                .fillMaxHeight()
                .pointerInput(enabled, slotPx, cards.size) {
                    if (!enabled) return@pointerInput
                    fun cardAt(x: Float) = (x / slotPx).toInt().coerceIn(0, cards.lastIndex)
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        var card = cardAt(down.position.x)
                        var scrubbing = false
                        pressedCard = card
                        while (true) {
                            val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            if (!scrubbing && abs(change.position.x - down.position.x) > viewConfiguration.touchSlop) {
                                scrubbing = true
                                jump(card)
                            }
                            if (scrubbing) {
                                change.consume()
                                val now = cardAt(change.position.x)
                                if (now != card) {
                                    card = now
                                    pressedCard = now
                                    jump(now)
                                }
                            }
                        }
                        pressedCard = null
                        if (!scrubbing) jump(card)
                    }
                }
        ) {
            cards.forEachIndexed { i, card ->
                val selected = i == activeCard
                Box(
                    Modifier
                        .width(slot)
                        .fillMaxHeight()
                        .semantics { onClick { onJump(i); true } },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .size(glyphSize)
                            .then(if (selected) Modifier.glow(card.accent, radius = 10.dp, alpha = 0.5f) else Modifier)
                            .clip(CircleShape)
                            .background(card.accent.copy(alpha = if (selected) 0.18f else 0f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            card.icon,
                            contentDescription = card.title(s),
                            tint = card.accent.copy(alpha = if (selected) 1f else 0.45f),
                            modifier = Modifier.size(glyphSize * 0.72f)
                        )
                    }
                }
            }
        }
        pressedCard?.let { i ->
            val card = cards[i]
            val centre = stripStart + slot * i + slot / 2
            val shape = RoundedCornerShape(10.dp)
            Row(
                Modifier
                    // Centred above the glyph but kept on screen, drawn over the bottom of the pager.
                    .layout { measurable, constraints ->
                        val p = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0, maxHeight = Constraints.Infinity))
                        val margin = 8.dp.roundToPx()
                        val x = (centre.roundToPx() - p.width / 2)
                            .coerceIn(margin, (constraints.maxWidth - p.width - margin).coerceAtLeast(margin))
                        layout(constraints.maxWidth, constraints.maxHeight) { p.place(x, -p.height - 4.dp.roundToPx()) }
                    }
                    .glow(card.accent, radius = 12.dp, cornerRadius = 10.dp, alpha = 0.45f)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, card.accent, shape)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(card.icon, contentDescription = null, tint = card.accent, modifier = Modifier.size(14.dp))
                Text(
                    card.stripLabel(s),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
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
            // An answer key: the official podium heads the three medal columns and each member's
            // pick sits under the medal they gave it.
            val density    = LocalDensity.current
            val leadWidth  = with(density) { GuessLeadWidth.toDp() }
            val pickWidth  = with(density) { GuessPickWidth.toDp() }
            val scoreWidth = with(density) { GuessScoreWidth.toDp() }
            GuessTableHeader(data, leadWidth, pickWidth, scoreWidth)
            Spacer(Modifier.height(6.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(8.dp))
            val scoredCount = data.guessScores.count { it.score > 0 }
            var scoredRank = 0
            var lastScore: Int? = null
            data.guessScores.forEachIndexed { i, gs ->
                if (i > 0 && i != scoredCount) Spacer(Modifier.height(4.dp))
                if (i == scoredCount && scoredCount > 0) {
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(10.dp))
                }
                // Dense ranking: ties share a rank, e.g. 1, 2, 2, 3 (not 1, 2, 2, 4).
                val rank = if (gs.score > 0) {
                    if (gs.score != lastScore) { scoredRank++; lastScore = gs.score }
                    scoredRank
                } else null
                GuessTableRow(gs, rank, data, leadWidth, pickWidth, scoreWidth,animVersion = animVersion, animDelayMs = (i * 70).toLong())
            }
            Spacer(Modifier.height(12.dp))
            Text(
                s.aftershowGuessScoringExplainer,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.28f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
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
private fun CompatibilityCard(data: AfterShowData, animVersion: Int = -1, sideInset: Dp = 16.dp, showInfo: Boolean = false) {
    val s = LocalAppStrings.current
    var infoOpen by remember { mutableStateOf(false) }
    if (infoOpen) {
        AlertDialog(
            onDismissRequest = { infoOpen = false },
            title = { Text(s.aftershowCompatibilityInfoTitle, style = MaterialTheme.typography.titleLarge) },
            text = { Text(s.aftershowCompatibilityInfoBody, Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = { TextButton(onClick = { infoOpen = false }) { Text(s.aftershowInfoClose) } }
        )
    }
    InfographicSection(
        s.aftershowCompatibility, GlowRose, sideInset = sideInset,
        // Only on screen; the share image leaves it out.
        titleInfo = if (showInfo) { { InfoButton { infoOpen = true } } } else null
    ) {
        val message = when {
            !data.hasVotes                  -> s.aftershowNoVotes
            !data.hasOwnVotes               -> s.aftershowCompatibilityNoOwnVotes
            data.compatibilities.isEmpty()  -> s.aftershowCompatibilityNoOthers
            else                            -> null
        }
        if (message != null) {
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            val best = data.compatibilities.first()
            CompatibilityHero(data.myUsername, best, animVersion)
            val others = data.compatibilities.drop(1)
            if (others.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(12.dp))
                others.forEachIndexed { i, c ->
                    CompatibilityRow(c, animVersion = animVersion, animDelayMs = (i * 60).toLong())
                    if (i < others.lastIndex) Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

// Small faint "?" beside a card title; the padding widens the tap target without enlarging the circle.
@Composable
private fun InfoButton(onClick: () -> Unit) {
    val faint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    Box(Modifier.noRippleClickable(onClick).padding(4.dp)) {
        Box(
            Modifier.size(16.dp).border(1.dp, faint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("?", fontSize = 10.sp, lineHeight = 10.sp, fontWeight = FontWeight.Bold, color = faint)
        }
    }
}

@Composable
private fun CompatibilityHero(myUsername: String?, best: Compatibility, animVersion: Int) {
    val s = LocalAppStrings.current
    val shown = remember(animVersion) { Animatable(if (animVersion == -1) best.percent.toFloat() else 0f) }
    LaunchedEffect(animVersion) {
        if (animVersion > 0 && animVersion % 2 == 1)
            shown.animateTo(best.percent.toFloat(), tween(900, easing = FastOutSlowInEasing))
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        // Your initials and your match's, overlapping like a pair.
        Box(Modifier.width(84.dp).height(52.dp)) {
            InitialsAvatar(myUsername ?: "?", GlowRose.copy(alpha = 0.45f), size = 48.dp, modifier = Modifier.align(Alignment.CenterStart))
            InitialsAvatar(best.username, GlowRose, size = 48.dp, glowing = true, modifier = Modifier.align(Alignment.CenterEnd))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                s.aftershowCompatibilityClosest,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp
            )
            Text(
                best.username,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        // The space is reserved for the final value (tabular digits, so no count-up value is
        // wider), keeping the text on the left from rewrapping while the number animates.
        val percentStyle = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, fontFeatureSettings = "tnum")
        Box(contentAlignment = Alignment.CenterEnd) {
            Text("${best.percent}%", style = percentStyle, modifier = Modifier.alpha(0f))
            Text("${shown.value.roundToInt()}%", style = percentStyle, color = GlowRose)
        }
    }
}

@Composable
private fun CompatibilityRow(c: Compatibility, animVersion: Int = -1, animDelayMs: Long = 0L) {
    val fraction = c.percent / 100f
    val animFraction = remember(animVersion) { Animatable(if (animVersion == -1) fraction else 0f) }
    LaunchedEffect(animVersion) {
        if (animVersion > 0 && animVersion % 2 == 1) {
            if (animDelayMs > 0L) delay(animDelayMs)
            animFraction.animateTo(fraction, tween(durationMillis = 600, easing = FastOutSlowInEasing))
        }
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        InitialsAvatar(c.username, GlowRose.copy(alpha = 0.3f + 0.7f * fraction), size = 30.dp)
        Spacer(Modifier.width(12.dp))
        Box(
            Modifier
                .weight(1f)
                .height(7.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(GlowRose.copy(alpha = 0.12f))
                .drawBehind {
                    drawRoundRect(
                        color = GlowRose.copy(alpha = 0.85f),
                        size = size.copy(width = size.width * animFraction.value.coerceIn(0f, 1f)),
                        cornerRadius = CornerRadius(99.dp.toPx())
                    )
                }
        )
        Spacer(Modifier.width(10.dp))
        Text(
            "${c.percent}%",
            style = MaterialTheme.typography.labelMedium,
            color = GlowRose,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(40.dp)
        )
    }
}

@Composable
private fun InitialsAvatar(name: String, color: Color, size: Dp, modifier: Modifier = Modifier, glowing: Boolean = false) {
    Box(
        modifier
            .then(if (glowing) Modifier.glow(color, radius = 12.dp, alpha = 0.5f) else Modifier)
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .background(color.copy(alpha = 0.18f))
            .border(2.dp, color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            name,
            fontSize = (size.value * 0.36f).sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
private fun MostGenerousCard(data: AfterShowData, animVersion: Int = -1, sideInset: Dp = 16.dp) {
    val s = LocalAppStrings.current
    InfographicSection(s.aftershowMostGenerous, GlowTeal, sideInset = sideInset) {
        if (!data.hasVotes) {
            Text(s.aftershowNoVotes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            val maxPts    = data.generousUsers.firstOrNull()?.second ?: 1
            // The bottom of the scale never sits above half the top total, so a small gap keeps the bars close.
            val minPts    = minOf(data.generousUsers.lastOrNull()?.second ?: 0, maxPts / 2)
            val range     = (maxPts - minPts).takeIf { it > 0 }?.toFloat() ?: 1f
            val lastIndex = data.generousUsers.lastIndex
            // The shortest bar is as wide as its content, so every row reserves the widest name and points label.
            val measurer  = rememberTextMeasurer()
            val density   = LocalDensity.current
            val nameStyle = MaterialTheme.typography.bodyMedium
            val ptsStyle  = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            val nameWidth = with(density) { data.generousUsers.maxOf { measurer.measure(it.first, nameStyle).size.width }.toDp() }
            val ptsWidth  = with(density) { data.generousUsers.maxOf { measurer.measure(s.aftershowPts(it.second), ptsStyle).size.width }.toDp() }
            data.generousUsers.forEachIndexed { i, (user, pts) ->
                val fraction = (pts - minPts) / range
                val warmth   = if (lastIndex > 0) 1f - i.toFloat() / lastIndex else 1f
                GenerousVoterBar(i + 1, user, pts, fraction, warmth, nameWidth, ptsWidth, animVersion = animVersion, animDelayMs = (i * 30).toLong())
                if (i < data.generousUsers.lastIndex) Spacer(Modifier.height(6.dp))
            }
        }
    }
}

// ── Group consensus ───────────────────────────────────────────────────────────

@Composable
private fun GroupConsensusCard(data: AfterShowData, animVersion: Int = -1, sideInset: Dp = 16.dp, showInfo: Boolean = false) {
    val s = LocalAppStrings.current
    var infoOpen by remember { mutableStateOf(false) }
    if (infoOpen) {
        AlertDialog(
            onDismissRequest = { infoOpen = false },
            title = { Text(s.aftershowConsensusInfoTitle, style = MaterialTheme.typography.titleLarge) },
            text = { Text(s.aftershowConsensusInfoBody, Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = { TextButton(onClick = { infoOpen = false }) { Text(s.aftershowInfoClose) } }
        )
    }
    InfographicSection(
        s.aftershowConsensus, GlowSky, sideInset = sideInset,
        // Only on screen; the share image leaves it out.
        titleInfo = if (showInfo) { { InfoButton { infoOpen = true } } } else null
    ) {
        val message = when {
            !data.hasVotes                -> s.aftershowNoVotes
            data.consensusVoterCount < 2  -> s.aftershowConsensusTooFewVoters
            data.consensusPicks.isEmpty() -> s.aftershowConsensusNoOverlap
            else                          -> null
        }
        if (message != null) {
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            ConsensusSubheading(s.aftershowConsensusPick)
            data.consensusPicks.forEachIndexed { i, ss ->
                SpreadRow(ss, data, scoreRange(ss), animVersion, animDelayMs = (i * 100).toLong())
                if (i < data.consensusPicks.lastIndex) Spacer(Modifier.height(6.dp))
            }
            if (data.mostDivisive.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(16.dp))
                ConsensusSubheading(s.aftershowMostDivisive)
                data.mostDivisive.forEachIndexed { i, ss ->
                    SpreadRow(ss, data, scoreRange(ss), animVersion, animDelayMs = (i * 100).toLong())
                    if (i < data.mostDivisive.lastIndex) Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

private val SpreadDotRadius = 3.dp

// Lowest–highest points, or just the points when everyone gave the same.
private fun scoreRange(ss: SongSpread): String =
    if (ss.points.first() == ss.points.last()) "${ss.points.first()}" else "${ss.points.first()}–${ss.points.last()}"

@Composable
private fun ConsensusSubheading(text: String) {
    Row(Modifier.fillMaxWidth().padding(bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(2.dp).height(13.dp).background(MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(6.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp
        )
    }
}

// Flag and name over a 1–12 strip with a dot for each score given; the dots spread out from the song's average.
@Composable
private fun SpreadRow(ss: SongSpread, data: AfterShowData, badge: String, animVersion: Int, animDelayMs: Long) {
    val s = LocalAppStrings.current
    val p = data.orderToParticipant[ss.order]
    val progress = remember(animVersion) { Animatable(if (animVersion == -1) 1f else 0f) }
    LaunchedEffect(animVersion) {
        if (animVersion > 0 && animVersion % 2 == 1) {
            if (animDelayMs > 0L) delay(animDelayMs)
            progress.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
        }
    }
    val dimColor = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        Modifier
            .fillMaxWidth()
            .background(GlowSky.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(if (p != null) countryFlag(p.country) else "🏳️", fontSize = 22.sp)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                if (p != null) s.translateCountry(p.country) else s.aftershowCountryFallback(ss.order),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // One dot per distinct score; when several members gave it, "+n" above the dot counts the others.
            val counts = remember(ss) { ss.points.groupingBy { it }.eachCount() }
            val hasTies = counts.values.any { it > 1 }
            val textMeasurer = rememberTextMeasurer()
            val tieStyle = TextStyle(fontSize = 9.sp, lineHeight = 10.sp, fontWeight = FontWeight.ExtraBold, color = GlowSky)
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(SpreadDotRadius * 2 + (if (hasTies) 11.dp else 0.dp) + 4.dp)
            ) {
                val dot = SpreadDotRadius.toPx()
                val left = dot
                val width = size.width - 2 * dot
                val cy = size.height - dot - 2.dp.toPx()
                val tick = 3.dp.toPx()
                drawLine(dimColor.copy(alpha = 0.25f), Offset(left, cy), Offset(left + width, cy), strokeWidth = 1.dp.toPx())
                listOf(left, left + width).forEach { x ->
                    drawLine(dimColor.copy(alpha = 0.35f), Offset(x, cy - tick), Offset(x, cy + tick), strokeWidth = 1.dp.toPx())
                }
                val dotAlpha = progress.value.coerceAtLeast(0.3f)
                counts.forEach { (pts, count) ->
                    val value = ss.mean + (pts - ss.mean) * progress.value
                    val x = left + width * ((value - 1) / 11f).toFloat()
                    drawCircle(GlowSky.copy(alpha = 0.9f * dotAlpha), dot, Offset(x, cy))
                    if (count > 1) {
                        val label = textMeasurer.measure("+${count - 1}", tieStyle)
                        drawText(
                            label,
                            topLeft = Offset(x - label.size.width / 2f, cy - dot - 1.dp.toPx() - label.size.height),
                            alpha = progress.value
                        )
                    }
                }
            }
        }
        Spacer(Modifier.width(10.dp))
        RankBadge(badge, GlowSky, width = 64.dp)
    }
}

// ── Awards ────────────────────────────────────────────────────────────────────

@Composable
private fun AwardsCard(
    data: AfterShowData,
    animVersion: Int = -1,
    sideInset: Dp = 16.dp,
    showInfo: Boolean = false,
    // On screen the other members' awards hide behind a toggle; the share image always shows them.
    collapsible: Boolean = false
) {
    val s = LocalAppStrings.current
    val mine = data.awards.filter { it.uid == data.myUid }
    // Everyone else's awards, one row per award with all its winners.
    val others = data.awards.filter { it.uid != data.myUid }.groupBy { it.kind }.toList()
    var othersOpen by remember { mutableStateOf(false) }
    var infoOpen by remember { mutableStateOf(false) }
    if (infoOpen) {
        AlertDialog(
            onDismissRequest = { infoOpen = false },
            title = { Text(s.aftershowAwardsInfoTitle, style = MaterialTheme.typography.titleLarge) },
            text = { Text(s.aftershowAwardsInfoBody, Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = { TextButton(onClick = { infoOpen = false }) { Text(s.aftershowInfoClose) } }
        )
    }
    InfographicSection(
        s.aftershowAwards, GlowFuchsia, sideInset = sideInset,
        // Only on screen; the share image leaves it out.
        titleInfo = if (showInfo) { { InfoButton { infoOpen = true } } } else null
    ) {
        // Your own awards lead; without any, a voter is told so and sees the others straight away.
        if (mine.isNotEmpty()) {
            mine.forEachIndexed { i, award ->
                AwardRow(award.kind, animVersion, animDelayMs = (i * 120).toLong(), number = awardNumber(award, data, s))
                Spacer(Modifier.height(8.dp))
            }
        } else if (data.hasOwnVotes) {
            Text(s.aftershowAwardsNoneForYou, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
        }
        if (others.isNotEmpty()) {
            val canCollapse = collapsible && mine.isNotEmpty()
            if (canCollapse) {
                Row(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(50))
                        .clickable { othersOpen = !othersOpen }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (othersOpen) s.aftershowAwardsHideOthers else s.aftershowAwardsShowOthers(others.size),
                        style = MaterialTheme.typography.labelLarge,
                        color = GlowFuchsia
                    )
                    Icon(
                        if (othersOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = GlowFuchsia,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else if (mine.isNotEmpty()) {
                Text(
                    s.aftershowAwardsOthers,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            AnimatedVisibility(
                visible = !canCollapse || othersOpen,
                enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                exit = shrinkVertically(tween(250)) + fadeOut(tween(200))
            ) {
                Column(Modifier.padding(top = if (canCollapse) 6.dp else 0.dp)) {
                    others.forEachIndexed { i, (kind, winners) ->
                        AwardRow(kind, animVersion, animDelayMs = ((mine.size + i) * 120).toLong(), winners = winners.map { it.username })
                        if (i < others.lastIndex) Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

private fun awardColor(kind: AwardKind): Color = when (kind) {
    AwardKind.Mainstream    -> GlowTeal
    AwardKind.Hipster       -> GlowPurple
    AwardKind.JuryBrain     -> GlowIndigo
    AwardKind.TelevoteHeart -> GlowRose
    AwardKind.Nostradamus   -> GlowGold
    AwardKind.SharpEye      -> GlowGreen
    AwardKind.Kingmaker     -> GlowGold
    AwardKind.LostCause     -> GlowPink
    AwardKind.HiveMind      -> GlowSky
    AwardKind.LoneWolf      -> GlowSilver
    AwardKind.Chatterbox    -> GlowOrange
}

private fun awardEmoji(kind: AwardKind): String = when (kind) {
    AwardKind.Mainstream    -> "📻"
    AwardKind.Hipster       -> "🕶️"
    AwardKind.JuryBrain     -> "🧠"
    AwardKind.TelevoteHeart -> "💖"
    AwardKind.Nostradamus   -> "🔮"
    AwardKind.SharpEye      -> "🎯"
    AwardKind.Kingmaker     -> "👑"
    AwardKind.LostCause     -> "💔"
    AwardKind.HiveMind      -> "🐝"
    AwardKind.LoneWolf      -> "🐺"
    AwardKind.Chatterbox    -> "💬"
}

private fun awardTitle(kind: AwardKind, s: AppStrings): String = when (kind) {
    AwardKind.Mainstream    -> s.aftershowAwardMainstream
    AwardKind.Hipster       -> s.aftershowAwardHipster
    AwardKind.JuryBrain     -> s.aftershowAwardJuryBrain
    AwardKind.TelevoteHeart -> s.aftershowAwardTelevoteHeart
    AwardKind.Nostradamus   -> s.aftershowAwardNostradamus
    AwardKind.SharpEye      -> s.aftershowAwardSharpEye
    AwardKind.Kingmaker     -> s.aftershowAwardKingmaker
    AwardKind.LostCause     -> s.aftershowAwardLostCause
    AwardKind.HiveMind      -> s.aftershowAwardHiveMind
    AwardKind.LoneWolf      -> s.aftershowAwardLoneWolf
    AwardKind.Chatterbox    -> s.aftershowAwardChatterbox
}

// What the award means, in a few words.
private fun awardAbout(kind: AwardKind, s: AppStrings): String = when (kind) {
    AwardKind.Mainstream    -> s.aftershowAwardMainstreamAbout
    AwardKind.Hipster       -> s.aftershowAwardHipsterAbout
    AwardKind.JuryBrain     -> s.aftershowAwardJuryBrainAbout
    AwardKind.TelevoteHeart -> s.aftershowAwardTelevoteHeartAbout
    AwardKind.Nostradamus   -> s.aftershowAwardNostradamusAbout
    AwardKind.SharpEye      -> s.aftershowAwardSharpEyeAbout
    AwardKind.Kingmaker     -> s.aftershowAwardKingmakerAbout
    AwardKind.LostCause     -> s.aftershowAwardLostCauseAbout
    AwardKind.HiveMind      -> s.aftershowAwardHiveMindAbout
    AwardKind.LoneWolf      -> s.aftershowAwardLoneWolfAbout
    AwardKind.Chatterbox    -> s.aftershowAwardChatterboxAbout
}

// The winner's own numbers behind it, shown on your own awards.
private fun awardNumber(award: Award, data: AfterShowData, s: AppStrings): String {
    fun country(order: Int) = data.orderToParticipant[order]?.country
        ?.let { "${countryFlag(it)} ${s.translateCountry(it)}" } ?: s.aftershowCountryFallback(order)
    return when (award.kind) {
        AwardKind.Mainstream    -> s.aftershowAwardMainstreamDetail(award.first)
        AwardKind.Hipster       -> s.aftershowAwardHipsterDetail(award.first)
        AwardKind.JuryBrain     -> s.aftershowAwardJuryBrainDetail(award.first, award.second)
        AwardKind.TelevoteHeart -> s.aftershowAwardTelevoteHeartDetail(award.first, award.second)
        AwardKind.Nostradamus   -> s.aftershowAwardNostradamusDetail(award.first)
        AwardKind.SharpEye      -> s.aftershowAwardSharpEyeDetail(award.first)
        AwardKind.Kingmaker     -> s.aftershowAwardKingmakerDetail(country(award.first))
        AwardKind.LostCause     -> s.aftershowAwardLostCauseDetail(country(award.first), award.second)
        AwardKind.HiveMind      -> s.aftershowAwardHiveMindDetail(award.first, award.second)
        AwardKind.LoneWolf      -> s.aftershowAwardLoneWolfDetail(award.first, award.second)
        AwardKind.Chatterbox    -> s.aftershowAwardChatterboxDetail(award.first)
    }
}

private const val AwardMaxAvatars = 3

// Badge, award name and what it means; the badge pops in when the card is shown. One of your own awards (with a
// number) is a bigger hero row with your numbers underneath; anyone else's row ends with the winners' initials.
@Composable
private fun AwardRow(
    kind: AwardKind,
    animVersion: Int,
    animDelayMs: Long,
    number: String? = null,
    winners: List<String> = emptyList()
) {
    val s = LocalAppStrings.current
    val hero = number != null
    val color = awardColor(kind)
    val pop = remember(animVersion) { Animatable(if (animVersion == -1) 1f else 0f) }
    LaunchedEffect(animVersion) {
        if (animVersion > 0 && animVersion % 2 == 1) {
            if (animDelayMs > 0L) delay(animDelayMs)
            pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        }
    }
    Row(
        Modifier
            .fillMaxWidth()
            .alpha(pop.value.coerceIn(0f, 1f))
            .background(color.copy(alpha = if (hero) 0.12f else 0.08f), RoundedCornerShape(12.dp))
            .then(if (hero) Modifier.border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(12.dp)) else Modifier)
            .padding(horizontal = 12.dp, vertical = if (hero) 12.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .scale(pop.value)
                .glow(color, radius = if (hero) 14.dp else 10.dp, alpha = if (hero) 0.5f else 0.35f)
                .size(if (hero) 52.dp else 40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .background(color.copy(alpha = 0.18f))
                .border(1.5.dp, color.copy(alpha = 0.7f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(awardEmoji(kind), fontSize = if (hero) 26.sp else 20.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                awardTitle(kind, s),
                style = if (hero) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp
            )
            Text(
                awardAbout(kind, s),
                fontSize = if (hero) 13.sp else 12.sp,
                lineHeight = if (hero) 16.sp else 15.sp,
                color = if (hero) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (number != null) {
                Text(
                    number,
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if (winners.isNotEmpty()) {
            Spacer(Modifier.width(8.dp))
            // Several winners overlap like the taste-twins pair; beyond three, "+n".
            val shown = winners.take(AwardMaxAvatars)
            val avatar = 32.dp
            val step = 20.dp
            Box(Modifier.width(avatar + step * (shown.size - 1)).height(avatar)) {
                shown.forEachIndexed { i, name ->
                    InitialsAvatar(name, color, size = avatar, glowing = i == 0, modifier = Modifier.offset(x = step * i))
                }
            }
            if (winners.size > AwardMaxAvatars) {
                Spacer(Modifier.width(4.dp))
                Text(
                    "+${winners.size - AwardMaxAvatars}",
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ── Where the points landed (map) ─────────────────────────────────────────────

@Composable
private fun PointsMapCard(
    data: AfterShowData,
    animVersion: Int = -1,
    sideInset: Dp = 16.dp,
    sideBySide: Boolean = false,
    spread: Float = DEFAULT_HEAT_SPREAD,
    onSpreadChange: ((Float) -> Unit)? = null
) {
    val s = LocalAppStrings.current
    val progress = remember(animVersion) { Animatable(if (animVersion == -1) 1f else 0f) }
    LaunchedEffect(animVersion) {
        if (animVersion > 0 && animVersion % 2 == 1) progress.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
    }
    val hasRoom = data.hasVotes

    InfographicSection(s.aftershowPointsMap, GlowIndigo, sideInset = sideInset) {
        if (sideBySide) {
            // Share image: both maps at once, each coloured against its own peak.
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (hasRoom) MapPanel(s.aftershowMapYourGroup, GlowPink, data.roomHeat, progress.value, Modifier.weight(1f))
                MapPanel(s.aftershowMapOfficial, GlowTeal, data.officialHeat, progress.value, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            HeatLegend(s.aftershowMapLessPoints, s.aftershowMapMorePoints)
        } else {
            var showEurope by remember(hasRoom) { mutableStateOf(!hasRoom) }
            val mix by animateFloatAsState(if (showEurope) 1f else 0f, tween(450), label = "mapMix")
            MapToggle(showEurope = showEurope, roomEnabled = hasRoom, onSelect = { showEurope = it })
            Spacer(Modifier.height(10.dp))
            PointsHeatMap(
                layers = listOf(HeatLayer(data.roomHeat, 1f - mix), HeatLayer(data.officialHeat, mix)),
                progress = progress.value,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            HeatLegend(s.aftershowMapLessPoints, s.aftershowMapMorePoints)
            if (onSpreadChange != null) SpreadSlider(spread, onSpreadChange)
        }
        if (data.offMapFlags.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                s.aftershowMapNotShown(data.offMapFlags),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MapPanel(label: String, color: Color, field: HeatField?, progress: Float, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp,
            color = color,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
        )
        PointsHeatMap(listOf(HeatLayer(field, 1f)), progress, Modifier.fillMaxWidth())
    }
}

@Composable
private fun MapToggle(showEurope: Boolean, roomEnabled: Boolean, onSelect: (Boolean) -> Unit) {
    val s = LocalAppStrings.current
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
            .padding(3.dp)
    ) {
        MapToggleOption(s.aftershowMapYourGroup, GlowPink, selected = !showEurope, enabled = roomEnabled, modifier = Modifier.weight(1f)) { onSelect(false) }
        MapToggleOption(s.aftershowMapOfficial, GlowTeal, selected = showEurope, enabled = true, modifier = Modifier.weight(1f)) { onSelect(true) }
    }
}

@Composable
private fun MapToggleOption(label: String, color: Color, selected: Boolean, enabled: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val bgAlpha by animateFloatAsState(if (selected) 0.16f else 0f, tween(200), label = "mapToggleBg")
    Box(
        modifier
            .alpha(if (enabled) 1f else 0.35f)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = bgAlpha))
            .border(1.dp, color.copy(alpha = bgAlpha * 3f), RoundedCornerShape(8.dp))
            .clickable(enabled = enabled && !selected, onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HeatLegend(startLabel: String, endLabel: String) {
    val ramp = remember {
        Brush.horizontalGradient(
            colorStops = HeatRampStops.map { (t, c) -> t to if (t < 0.05f) c.copy(alpha = 0.35f) else c }.toTypedArray()
        )
    }
    Box(Modifier.fillMaxWidth().height(6.dp).background(ramp, RoundedCornerShape(3.dp)))
    Spacer(Modifier.height(4.dp))
    Row(Modifier.fillMaxWidth()) {
        Text(startLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.weight(1f))
        Text(endLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
    }
}

@Composable
private fun SpreadSlider(spread: Float, onSpreadChange: (Float) -> Unit) {
    val s = LocalAppStrings.current
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    Spacer(Modifier.height(6.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(s.aftershowMapResolution, style = MaterialTheme.typography.labelSmall, color = labelColor)
        Slider(
            value = spread,
            onValueChange = onSpreadChange,
            valueRange = HeatSpreadRange,
            colors = SliderDefaults.colors(
                thumbColor = GlowIndigo,
                activeTrackColor = GlowIndigo.copy(alpha = 0.7f),
                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier.weight(1f).padding(horizontal = 10.dp)
        )
        Text(
            s.aftershowMapSpreadKm(heatSpreadKm(spread)),
            style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.width(56.dp)
        )
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
        PointsMapCard(data, sideBySide = true)
        // Half insets on the cards plus half on the columns: 16 dp at the edges and between the columns.
        BalancedTwoColumns(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            GuessedWinnersCard(data, sideInset = 8.dp)
            MostGenerousCard(data, sideInset = 8.dp)
            SharedFeelingsCard(data, sideInset = 8.dp)
            CompatibilityCard(data, sideInset = 8.dp)
            GroupConsensusCard(data, sideInset = 8.dp)
            RankShiftCard(data, sideInset = 8.dp)
            if (data.awards.isNotEmpty()) AwardsCard(data, sideInset = 8.dp)
            OfficialResultsCard(data, sideInset = 8.dp)
        }

        if (showFooter) {
            Spacer(Modifier.height(24.dp))
            ExportLogo(Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(20.dp))
        }
    }
}

// Splits the children into two columns, keeping their order within each column, choosing the split whose column
// heights are closest (ties go to the split with more cards on the left). Tries every split, fine for a handful of cards.
@Composable
private fun BalancedTwoColumns(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(content, modifier) { measurables, constraints ->
        val columnWidth = constraints.maxWidth / 2
        val items = measurables.map { it.measure(Constraints.fixedWidth(columnWidth)) }
        var bestMask = 0
        var bestDiff = Int.MAX_VALUE
        for (mask in 0 until (1 shl items.size)) {
            var left = 0
            var right = 0
            items.forEachIndexed { i, item -> if ((mask shr i) and 1 == 1) right += item.height else left += item.height }
            if (abs(left - right) < bestDiff) {
                bestDiff = abs(left - right)
                bestMask = mask
            }
        }
        val inRight = items.indices.map { (bestMask shr it) and 1 == 1 }
        val leftHeight = items.indices.filter { !inRight[it] }.sumOf { items[it].height }
        val rightHeight = items.indices.filter { inRight[it] }.sumOf { items[it].height }
        layout(constraints.maxWidth, maxOf(leftHeight, rightHeight)) {
            var leftY = 0
            var rightY = 0
            items.forEachIndexed { i, item ->
                if (inRight[i]) {
                    item.place(constraints.maxWidth - columnWidth, rightY)
                    rightY += item.height
                } else {
                    item.place(0, leftY)
                    leftY += item.height
                }
            }
        }
    }
}

// The plain logo has the app background baked in (matching the All cards image); the
// transparent one is for backgrounds with gradients or sparkles behind it.
@Composable
private fun ExportLogo(modifier: Modifier = Modifier, height: Dp = 28.dp, transparent: Boolean = false) {
    Image(
        painter = painterResource(if (transparent) R.drawable.est_banner_short_transparent else R.drawable.est_banner_short),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier.height(height)
    )
}

// ── Summary card (single 9:16 share image) ───────────────────────────────────

private val SummaryCardWidth  = 360.dp
private val SummaryCardHeight = 640.dp
private const val SummaryCardPxPerDp = 3f   // 360 × 640 dp → 1080 × 1920 px
private val SummaryGap = 6.dp               // minimum space between panels, both ways

private class SummaryTile(
    val label: String,
    val accent: Color,
    val headline: String,
    val detail: @Composable () -> Unit
)

// The most telling one-liners from the cards, in order of interest; tiles that don't apply are left out.
@Composable
private fun summaryTiles(data: AfterShowData): List<SummaryTile> {
    val s = LocalAppStrings.current
    if (!data.hasVotes) return emptyList()
    fun countryLine(order: Int): String {
        val p = data.orderToParticipant[order]
        return if (p != null) "${countryFlag(p.country)}  ${s.translateCountry(p.country)}" else s.aftershowCountryFallback(order)
    }
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    fun rankShiftTile(label: String, rs: RobScore) = SummaryTile(label, GlowOrange, countryLine(rs.order)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SummaryDetailText("${s.aftershowColGroup} #${data.userRanks[rs.order] ?: 0}", GlowOrange)
            SummaryDetailText("${s.aftershowColOfficial} #${data.officialRanks[rs.order] ?: 0}", muted)
        }
    }

    val bestGuessers = data.guessScores.filter { it.score > 0 }.let { scored ->
        scored.filter { it.score == scored.firstOrNull()?.score }
    }
    val closest = data.userOverlaps.maxByOrNull { it.top10 * 10 + it.top5 }
    val generous = data.generousUsers.firstOrNull()

    return listOfNotNull(
        bestGuessers.takeIf { it.isNotEmpty() }?.let { winners ->
            SummaryTile(s.aftershowSummaryBestGuesser, GlowPurple, namesLine(winners.map { it.username })) {
                SummaryDetailText(s.aftershowPts(winners.first().score), GlowPurple)
            }
        },
        closest?.takeIf { it.top10 > 0 }?.let { uo ->
            SummaryTile(s.aftershowSummaryClosestMatch, GlowPink, uo.username) {
                SummaryDetailText(s.aftershowSummaryTop10Match(uo.top10), GlowPink)
            }
        },
        data.biggestSurprise.firstOrNull()?.takeIf { it.diff > 0 }?.let { rankShiftTile(s.aftershowBiggestSurprise, it) },
        data.mostRobbed.firstOrNull()?.takeIf { it.diff < 0 }?.let { rankShiftTile(s.aftershowMostRobbed, it) },
        data.mostDivisive.firstOrNull()?.let { ss ->
            SummaryTile(s.aftershowMostDivisive, GlowSky, countryLine(ss.order)) {
                SummaryDetailText(scoreRange(ss), GlowSky)
            }
        },
        generous?.let { (user, pts) ->
            SummaryTile(s.aftershowSummaryMostGenerous, GlowTeal, user) {
                SummaryDetailText(s.aftershowPts(pts), GlowTeal)
            }
        }
    )
}

// Ties are all named, up to three; beyond that, "AB, CD, EF +2".
private fun namesLine(names: List<String>): String =
    if (names.size <= 3) names.joinToString(", ") else names.take(3).joinToString(", ") + " +${names.size - 3}"

@Composable
private fun SummaryCard(data: AfterShowData, modifier: Modifier = Modifier) {
    val s = LocalAppStrings.current
    val background = MaterialTheme.colorScheme.background
    val tiles = summaryTiles(data)
    Column(
        modifier
            .background(background)
            .drawBehind {
                drawRect(Brush.radialGradient(listOf(GlowPurple.copy(alpha = 0.22f), Color.Transparent), center = Offset(size.width * 0.5f, 0f), radius = size.width * 0.9f))
                drawRect(Brush.radialGradient(listOf(GlowTeal.copy(alpha = 0.12f), Color.Transparent), center = Offset(size.width, size.height), radius = size.width * 0.9f))
                // A still frame of the screen's drifting sparkles.
                val rng = kotlin.random.Random(42L)
                repeat(70) {
                    val x = rng.nextFloat() * size.width
                    val y = rng.nextFloat() * size.height
                    val r = (rng.nextFloat() * 3.2f + 1.2f) * density / 2.5f
                    val a = rng.nextFloat() * 0.18f + 0.06f
                    drawCircle(GlowPurple.copy(alpha = a), radius = r, center = Offset(x, y))
                }
            }
            .padding(start = 18.dp, end = 18.dp, top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AftershowLockup("${s.aftershowResultsLabel} · ${data.year}", titleSize = 27.sp, shine = false)
        Spacer(Modifier.height(12.dp))
        Spacer(Modifier.weight(1f))
        SummaryPodium(data)
        // Panels are always at least SummaryGap apart; any spare height is shared out on top.
        Spacer(Modifier.height(SummaryGap))
        Spacer(Modifier.weight(1f))
        SummaryMaps(data)
        if (data.hasVotes) {
            Spacer(Modifier.height(SummaryGap))
            Spacer(Modifier.weight(1f))
            SummarySharedFavorites(data)
        }
        if (tiles.isNotEmpty()) {
            Spacer(Modifier.height(SummaryGap))
            Spacer(Modifier.weight(1f))
            tiles.chunked(2).forEachIndexed { i, rowTiles ->
                if (i > 0) Spacer(Modifier.height(SummaryGap))
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(SummaryGap)) {
                    // An odd last tile spans the whole row.
                    rowTiles.forEach { SummaryTileBox(it, Modifier.weight(1f).fillMaxHeight()) }
                }
            }
        }
        // The logo has a fixed slot (so a full card can't squeeze it) and sits centred in the
        // leftover bottom space, so the gap above and below it is equal.
        Spacer(Modifier.weight(1.5f))
        ExportLogo(Modifier.padding(vertical = 10.dp), height = 22.dp, transparent = true)
        Spacer(Modifier.weight(1.5f))
    }
}

@Composable
private fun SummaryPanel(modifier: Modifier = Modifier, accent: Color, verticalPadding: Dp = 10.dp, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = verticalPadding),
        content = content
    )
}

@Composable
private fun SummaryLabel(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        fontSize = 9.5.sp,
        lineHeight = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.8.sp,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Composable
private fun SummaryDetailText(text: String, color: Color) {
    Text(text, fontSize = 11.sp, lineHeight = 13.sp, fontWeight = FontWeight.Bold, color = color, maxLines = 1)
}

// Official top three as a podium (2 · 1 · 3), each with where the group ranked it.
@Composable
private fun SummaryPodium(data: AfterShowData) {
    val s = LocalAppStrings.current
    SummaryPanel(accent = GlowGreen) {
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            listOf(2 to "🥈", 1 to "🥇", 3 to "🥉").forEach { (rank, medal) ->
                val order = data.officialTop3ByRank[rank]
                val p = order?.let { data.orderToParticipant[it] }
                val isWinner = rank == 1
                Column(Modifier.weight(if (isWinner) 1.2f else 1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box {
                        Text(if (p != null) countryFlag(p.country) else "🏳️", fontSize = if (isWinner) 34.sp else 26.sp, lineHeight = if (isWinner) 38.sp else 30.sp)
                        Text(
                            medal,
                            fontSize = if (isWinner) 15.sp else 12.sp,
                            modifier = Modifier.align(Alignment.TopStart).offset(x = if (isWinner) (-12).dp else (-10).dp, y = (-4).dp)
                        )
                    }
                    Text(
                        when {
                            p != null     -> s.translateCountry(p.country)
                            order != null -> s.aftershowCountryFallback(order)
                            else          -> ""
                        },
                        fontSize = if (isWinner) 14.sp else 12.sp,
                        fontWeight = if (isWinner) FontWeight.ExtraBold else FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Unranked = the group gave it no points.
                    if (data.hasVotes && order != null) SummaryDetailText(s.aftershowSummaryGroupRank(data.userRanks[order]), GlowOrange)
                }
            }
        }
    }
}

// Room and official heat maps side by side; official only when the room hasn't voted.
@Composable
private fun SummaryMaps(data: AfterShowData) {
    val s = LocalAppStrings.current
    SummaryPanel(accent = GlowIndigo) {
        // Fixed widths keep the maps short enough to leave room for three rows of tiles.
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)) {
            if (data.hasVotes) SummaryMapPanel(s.aftershowMapYourGroup, GlowPink, data.roomHeat, Modifier.width(120.dp))
            SummaryMapPanel(s.aftershowMapOfficial, GlowTeal, data.officialHeat, Modifier.width(if (data.hasVotes) 120.dp else 220.dp))
        }
    }
}

@Composable
private fun SummaryMapPanel(label: String, color: Color, field: HeatField?, modifier: Modifier = Modifier) {
    Column(modifier) {
        SummaryLabel(label, color, Modifier.padding(bottom = 4.dp))
        PointsHeatMap(listOf(HeatLayer(field, 1f)), progress = 1f, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun SummarySharedFavorites(data: AfterShowData) {
    val s = LocalAppStrings.current
    SummaryPanel(accent = GlowPink) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                SummaryLabel(s.aftershowSharedFeelings, GlowPink)
                Spacer(Modifier.height(2.dp))
                Text(
                    s.aftershowSummaryOverlapHint,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    maxLines = 1
                )
            }
            Spacer(Modifier.width(8.dp))
            SummaryStat("${data.groupOverlap10}/10", "TOP 10", GlowPurple)
            Spacer(Modifier.width(14.dp))
            SummaryStat("${data.groupOverlap5}/5", "TOP 5", GlowPink)
        }
    }
}

@Composable
private fun SummaryStat(stat: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            stat,
            fontSize = 18.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color,
            style = TextStyle(shadow = Shadow(color = color, offset = Offset.Zero, blurRadius = 16f))
        )
        Text(label, fontSize = 9.sp, lineHeight = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f))
    }
}

@Composable
private fun SummaryTileBox(tile: SummaryTile, modifier: Modifier = Modifier) {
    SummaryPanel(modifier, accent = tile.accent, verticalPadding = 8.dp) {
        SummaryLabel(tile.label, tile.accent)
        Spacer(Modifier.height(4.dp))
        Text(tile.headline, fontSize = 14.sp, lineHeight = 17.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(2.dp))
        tile.detail()
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
            .padding(start = 24.dp, end = 24.dp, top = 10.dp, bottom = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        AftershowLockup("${LocalAppStrings.current.aftershowResultsLabel} · $year")
    }
}

// The app logo's "SCORE TRACKER" gradient (est_banner): cyan → blue → purple → pink.
private val BrandGradientColors = listOf(Color(0xFF1FD8F2), Color(0xFF5A82F6), GlowPurple, GlowPink)

// A light band sweeps across the subtitle once per cycle, then rests for the remainder.
private const val LockupShineCycleMs = 6000
private const val LockupShineSweepMs = 1800

// Aftershow title in the style of the app logo's word mark: white spaced caps over a
// gradient subtitle. Used by the hero, the coming-soon header and the summary share image.
@Composable
private fun AftershowLockup(subtitle: String, modifier: Modifier = Modifier, titleSize: TextUnit = 24.sp, shine: Boolean = true) {
    val base = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    // Read only in the draw phase, so the sweep redraws the subtitle without recomposing.
    val shineCycle = if (shine) {
        rememberInfiniteTransition(label = "lockupShine").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(LockupShineCycleMs, easing = LinearEasing)),
            label = "lockupShineT"
        )
    } else null
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "AFTERSHOW",
            style = base.copy(fontSize = titleSize, lineHeight = titleSize, letterSpacing = 0.32.em),
            color = Color.White
        )
        Spacer(Modifier.height(2.dp))
        Text(
            subtitle,
            style = base.copy(
                fontSize = titleSize * 0.5f, lineHeight = titleSize * 0.6f, letterSpacing = 0.44.em,
                brush = Brush.horizontalGradient(BrandGradientColors)
            ),
            modifier = Modifier
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .drawWithContent {
                    drawContent()
                    val p = (shineCycle?.value ?: return@drawWithContent) * LockupShineCycleMs / LockupShineSweepMs
                    if (p >= 1f) return@drawWithContent
                    val band = size.width * 0.35f
                    val x = -band + p * (size.width + band)
                    drawRect(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, Color.White.copy(alpha = 0.6f), Color.Transparent),
                            startX = x, endX = x + band
                        ),
                        blendMode = BlendMode.SrcAtop
                    )
                }
        )
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
    titleInfo: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) = InfographicSection(icon = "", title = title, accentColor = accentColor, sideInset = sideInset, titleTrailing = titleTrailing, titleBottomPadding = titleBottomPadding, titleInfo = titleInfo, content = content)

@Composable
private fun InfographicSection(
    titleSegments: List<Pair<String, Color>>,
    accentColor: Color,
    sideInset: Dp = 16.dp,
    titleTrailing: (@Composable () -> Unit)? = null,
    titleBottomPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) = InfographicSection(icon = "", title = "", accentColor = accentColor, sideInset = sideInset, titleTrailing = titleTrailing, titleBottomPadding = titleBottomPadding, titleSegments = titleSegments, content = content)

@Composable
private fun InfographicSection(
    icon: String,
    title: String,
    accentColor: Color,
    sideInset: Dp = 16.dp,
    titleTrailing: (@Composable () -> Unit)? = null,
    titleBottomPadding: Dp = 16.dp,
    titleSegments: List<Pair<String, Color>>? = null,
    titleInfo: (@Composable () -> Unit)? = null,
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
                    val segments = titleSegments ?: listOf(title to accentColor)
                    segments.forEachIndexed { i, (text, color) ->
                        Text(
                            text,
                            style = MaterialTheme.typography.titleSmall,
                            color = color,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        if (i < segments.lastIndex) Spacer(Modifier.width(4.dp))
                    }
                    if (titleInfo != null) {
                        Spacer(Modifier.width(6.dp))
                        titleInfo()
                    }
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

// The rank + name and score columns are sized in sp so they grow with the system font size and
// stay the same width in every row. The three medal columns are grouped in the middle, each up to
// GuessPickWidth wide, and share the width left over when that isn't enough.
private val GuessLeadWidth  = 58.sp
private val GuessScoreWidth = 36.sp
private val GuessPickWidth  = 56.sp
private val GuessRowPadding = 8.dp
private val guessMedals     = listOf("🥇", "🥈", "🥉")

// The middle of a guess table row: three medal columns, each up to pickWidth wide, centred as a group.
@Composable
private fun RowScope.GuessPickColumns(pickWidth: Dp, verticalAlignment: Alignment.Vertical, cell: @Composable (pickRank: Int) -> Unit) {
    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Center, verticalAlignment = verticalAlignment) {
        (1..3).forEach { pickRank ->
            Box(Modifier.weight(1f, fill = false).width(pickWidth), contentAlignment = Alignment.Center) { cell(pickRank) }
        }
    }
}

@Composable
private fun GuessTableHeader(data: AfterShowData, leadWidth: Dp, pickWidth: Dp, scoreWidth: Dp) {
    val s     = LocalAppStrings.current
    val faint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = GuessRowPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(leadWidth))
        GuessPickColumns(pickWidth, Alignment.CenterVertically) { pickRank ->
            val participant = data.officialTop3ByRank[pickRank]?.let { data.orderToParticipant[it] }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(guessMedals[pickRank - 1], fontSize = 12.sp, lineHeight = 18.sp)
                Text(if (participant != null) countryFlag(participant.country) else "🏳️", fontSize = 16.sp, lineHeight = 18.sp)
            }
        }
        Text(
            s.aftershowGuessScoreUnit.uppercase(),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = faint,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.width(scoreWidth).padding(bottom = 2.dp)
        )
    }
}

@Composable
private fun GuessTableRow(
    gs: GuessScore,
    rankInLeaderboard: Int?,
    data: AfterShowData,
    leadWidth: Dp,
    pickWidth: Dp,
    scoreWidth: Dp,
    animVersion: Int = -1,
    animDelayMs: Long = 0L
) {
    val isZero       = rankInLeaderboard == null
    val scoreColor   = if (!isZero) guessRankColor(rankInLeaderboard!!)
                       else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    val targetAlpha  = if (isZero) 0.65f else 1f
    val offsetY      = remember(animVersion) { Animatable(if (animVersion == -1) 0f else 10f) }
    val blockAlpha   = remember(animVersion) { Animatable(if (animVersion == -1) targetAlpha else 0f) }
    LaunchedEffect(animVersion) {
        if (animVersion > 0 && animVersion % 2 == 1) {
            if (animDelayMs > 0L) delay(animDelayMs)
            launch { offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
            blockAlpha.animateTo(targetAlpha, tween(durationMillis = 220))
        }
    }
    val badgeSize    = with(LocalDensity.current) { 20.sp.toDp() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = offsetY.value.dp)
            .alpha(blockAlpha.value)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isZero) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = GuessRowPadding, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.width(leadWidth),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.size(badgeSize).background(scoreColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (rankInLeaderboard != null) "$rankInLeaderboard" else "–",
                    fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black.copy(alpha = 0.75f)
                )
            }
            Text(gs.username, fontSize = 14.sp, fontWeight = if (isZero) FontWeight.Normal else FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Clip)
        }
        GuessPickColumns(pickWidth, Alignment.CenterVertically) { pickRank ->
            val order = gs.picks[pickRank]
            if (order == null) {
                Text("–", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            } else {
                val participant = data.orderToParticipant[order]
                GuessPickCell(
                    flag   = if (participant != null) countryFlag(participant.country) else "🏳️",
                    result = pickResult(pickRank, order, data.officialTop3ByRank, data.officialTop3Orders)
                )
            }
        }
        Box(Modifier.width(scoreWidth), contentAlignment = Alignment.Center) {
            val scoreFontSize = if (isZero) 14.sp else 15.sp
            Text(
                "${gs.score}",
                fontSize = scoreFontSize,
                fontWeight = FontWeight.ExtraBold,
                color = scoreColor,
                lineHeight = scoreFontSize,
                maxLines = 1,
                modifier = Modifier
                    .background(scoreColor.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
                    .border(1.dp, scoreColor.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
    }
}

// One pick under its medal column: a correct pick sits on a tint of its result accent, a miss is greyed out.
@Composable
private fun GuessPickCell(flag: String, result: PickResult) {
    val look = if (result == PickResult.MISS) {
        Modifier.alpha(0.45f)
    } else {
        Modifier.background(pickResultTint(result), RoundedCornerShape(8.dp))
    }
    Text(flag, fontSize = 16.sp, lineHeight = 18.sp, modifier = look.padding(horizontal = 8.dp, vertical = 3.dp))
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
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.width(30.dp.fontScaled()),
            textAlign = TextAlign.End
        )
    }
}

// ── Generous voters ───────────────────────────────────────────────────────────

@Composable
private fun GenerousVoterBar(rank: Int, user: String, pts: Int, fraction: Float, warmth: Float, nameWidth: Dp, ptsWidth: Dp, animVersion: Int = -1, animDelayMs: Long = 0L) {
    val s = LocalAppStrings.current
    val color = generousVoterColor(warmth)
    val animFraction = remember(animVersion) { Animatable(if (animVersion == -1) fraction else 0f) }
    LaunchedEffect(animVersion) {
        if (animVersion > 0 && animVersion % 2 == 1) {
            if (animDelayMs > 0L) delay(animDelayMs)
            animFraction.animateTo(fraction, tween(durationMillis = 350, easing = FastOutSlowInEasing))
        }
    }
    // The bar is the row itself, so the points sit at its end. Its width runs from the content
    // width (fraction 0) to the full width (fraction 1).
    Row(
        modifier = Modifier
            .layout { measurable, constraints ->
                val content  = measurable.maxIntrinsicWidth(constraints.maxHeight).coerceAtMost(constraints.maxWidth)
                val width    = content + ((constraints.maxWidth - content) * animFraction.value).toInt()
                val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
                layout(placeable.width, placeable.height) { placeable.place(0, 0) }
            }
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.horizontalGradient(listOf(color.copy(alpha = 0.40f), color.copy(alpha = 0.12f))),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Box(
                modifier = Modifier.size(22.dp).background(color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("$rank", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black.copy(alpha = 0.75f))
            }
            Text(user, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.widthIn(min = nameWidth))
        }
        Text(
            s.aftershowPts(pts),
            style = MaterialTheme.typography.labelMedium,
            color = color.copy(alpha = 0.85f),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(start = 10.dp)
                .background(color.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                .padding(horizontal = 9.dp, vertical = 3.dp)
                .widthIn(min = ptsWidth)
        )
    }
}

// ── Official results table ────────────────────────────────────────────────────

private enum class ResultSortColumn { RANK, GROUP, JURY, PUBLIC, TOTAL }

private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
}

// Hint animation that chases a shine across the header row, left to right, once the
// card has been open a moment, to suggest the columns are tappable.
private const val HeaderShineColumnCount = 6
private const val HeaderShineStartDelayMs = 500L
private const val HeaderShineTotalMs = 2000
private const val HeaderShineStaggerMs = 200
private const val HeaderShineCycleMs = 1000

@Composable
private fun OfficialResultsCard(data: AfterShowData, animVersion: Int = -1, sideInset: Dp = 16.dp) {
    val s = LocalAppStrings.current
    var sortColumn by remember { mutableStateOf(ResultSortColumn.RANK) }
    var sortAscending by remember { mutableStateOf(true) }
    val onColumnClick: (ResultSortColumn) -> Unit = { col ->
        if (sortColumn == col) {
            sortAscending = !sortAscending
        } else {
            sortColumn = col
            sortAscending = col == ResultSortColumn.RANK || col == ResultSortColumn.GROUP
        }
    }
    fun countryName(entry: CountryResult): String {
        val p = data.orderToParticipant[entry.order]
        return if (p != null) s.translateCountry(p.country) else s.aftershowCountryFallback(entry.order)
    }
    val shineProgress = remember(animVersion) { Animatable(0f) }
    LaunchedEffect(animVersion) {
        if (animVersion > 0 && animVersion % 2 == 1) {
            delay(HeaderShineStartDelayMs)
            shineProgress.snapTo(0f)
            shineProgress.animateTo(1f, tween(HeaderShineTotalMs, easing = LinearEasing))
        } else {
            shineProgress.snapTo(0f)
        }
    }
    fun shineIntensity(columnIndex: Int): Float {
        val elapsed = shineProgress.value * HeaderShineTotalMs
        val local = elapsed - columnIndex * HeaderShineStaggerMs
        return (if (local in 0f..HeaderShineCycleMs.toFloat()) sin(PI * local / HeaderShineCycleMs).toFloat() else 0f)
            .coerceIn(0f, 1f)
    }
    InfographicSection(
        titleSegments = listOf(
            s.aftershowOfficialLabel to GlowGreen,
            "&" to MaterialTheme.colorScheme.onSurfaceVariant,
            s.aftershowOfficialTitleGroup.uppercase() to GlowOrange,
            s.aftershowResultsLabel to MaterialTheme.colorScheme.onSurfaceVariant
        ),
        accentColor = GlowGreen,
        sideInset = sideInset
    ) {
        Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), verticalAlignment = Alignment.Top) {
            Row(modifier = Modifier.width(50.dp)) {
                SortableHeaderLabel(
                    "#", ResultSortColumn.RANK, sortColumn, sortAscending, GlowGreen,
                    modifier = Modifier.width(22.dp), onClick = onColumnClick,
                    shineIntensity = shineIntensity(0)
                )
                Spacer(Modifier.width(4.dp))
                SortableHeaderLabel(
                    "#", ResultSortColumn.GROUP, sortColumn, sortAscending, GlowOrange,
                    onClick = onColumnClick, shineIntensity = shineIntensity(1)
                )
            }
            Spacer(Modifier.width(10.dp))
            PlainHeaderLabel(
                s.countryHeader,
                MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                shineIntensity = shineIntensity(2)
            )
            SortableHeaderLabel(
                s.aftershowColJury, ResultSortColumn.JURY, sortColumn, sortAscending,
                MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(60.dp),
                textAlign = TextAlign.Center, onClick = onColumnClick,
                shineIntensity = shineIntensity(3)
            )
            SortableHeaderLabel(
                s.aftershowColPublic, ResultSortColumn.PUBLIC, sortColumn, sortAscending,
                MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(60.dp),
                textAlign = TextAlign.Center, onClick = onColumnClick,
                shineIntensity = shineIntensity(4)
            )
            SortableHeaderLabel(
                s.aftershowColTotal, ResultSortColumn.TOTAL, sortColumn, sortAscending,
                MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(60.dp),
                textAlign = TextAlign.Center, onClick = onColumnClick,
                shineIntensity = shineIntensity(5)
            )
        }
        val comparator: Comparator<CountryResult> = when (sortColumn) {
            ResultSortColumn.RANK    -> compareBy { it.rank }
            ResultSortColumn.GROUP   -> compareBy { data.userRanks[it.order] ?: Int.MAX_VALUE }
            ResultSortColumn.JURY    -> compareBy { it.juryScore }
            ResultSortColumn.PUBLIC  -> compareBy { it.publicScore }
            ResultSortColumn.TOTAL   -> compareBy { it.juryScore + it.publicScore }
        }
        val ordered = data.officialEntries.sortedWith(comparator)
        val sorted = if (sortAscending) ordered else ordered.reversed()
        sorted.forEachIndexed { i, entry ->
            val p = data.orderToParticipant[entry.order]
            OfficialResultRow(
                rank        = entry.rank,
                flag        = if (p != null) countryFlag(p.country) else "🏳️",
                name        = countryName(entry),
                juryScore   = entry.juryScore,
                publicScore = entry.publicScore,
                groupRank   = data.userRanks[entry.order],
                onColumnClick = onColumnClick,
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

private val HeaderArrowSlotHeight = 15.dp

@Composable
private fun SortableHeaderLabel(
    text: String,
    column: ResultSortColumn,
    activeColumn: ResultSortColumn,
    ascending: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    shineIntensity: Float = 0f,
    onClick: (ResultSortColumn) -> Unit
) {
    val active = column == activeColumn
    val boxAlignment = when (textAlign) {
        TextAlign.End -> Alignment.CenterEnd
        TextAlign.Center -> Alignment.Center
        else -> Alignment.CenterStart
    }
    Box(
        modifier = modifier.noRippleClickable { onClick(column) },
        contentAlignment = boxAlignment
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.height(HeaderArrowSlotHeight), contentAlignment = Alignment.Center) {
                if (active) {
                    Text(
                        if (ascending) "▲" else "▼",
                        fontSize = 13.sp,
                        lineHeight = 13.sp,
                        color = color,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            HeaderShineText(
                text = text,
                color = if (active) color else color.copy(alpha = 0.55f),
                shineIntensity = shineIntensity
            )
        }
    }
}

@Composable
private fun PlainHeaderLabel(text: String, color: Color, modifier: Modifier = Modifier, shineIntensity: Float = 0f) {
    Column(modifier = modifier) {
        Spacer(Modifier.height(HeaderArrowSlotHeight))
        HeaderShineText(text = text, color = color, shineIntensity = shineIntensity)
    }
}

@Composable
private fun HeaderShineText(text: String, color: Color, shineIntensity: Float) {
    val glowRadiusPx = with(LocalDensity.current) { 6.dp.toPx() }
    Text(
        text,
        style = MaterialTheme.typography.bodySmall.copy(
            shadow = if (shineIntensity > 0f)
                Shadow(color = Color.White.copy(alpha = shineIntensity), offset = Offset.Zero, blurRadius = glowRadiusPx)
            else null
        ),
        color = lerp(color, Color.White, shineIntensity * 0.6f),
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.8.sp,
        maxLines = 1
    )
}

@Composable
private fun OfficialResultRow(
    rank: Int,
    flag: String,
    name: String,
    juryScore: Int,
    publicScore: Int,
    groupRank: Int?,
    onColumnClick: (ResultSortColumn) -> Unit,
    animVersion: Int = -1,
    animDelayMs: Long = 0L
) {
    val total = juryScore + publicScore
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
        Row(modifier = Modifier.width(50.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "$rank",
                modifier = Modifier.width(22.dp).noRippleClickable { onColumnClick(ResultSortColumn.RANK) },
                style = MaterialTheme.typography.labelMedium,
                color = GlowGreen,
                fontWeight = if (rank <= 3) FontWeight.ExtraBold else FontWeight.Normal
            )
            Spacer(Modifier.width(4.dp))
            Text(
                if (groupRank != null) "$groupRank" else "–",
                modifier = Modifier.noRippleClickable { onColumnClick(ResultSortColumn.GROUP) },
                style = MaterialTheme.typography.labelMedium,
                color = GlowOrange,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(Modifier.width(10.dp))
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
            modifier = Modifier.width(60.dp).noRippleClickable { onColumnClick(ResultSortColumn.JURY) },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Text(
            "$publicScore",
            modifier = Modifier.width(60.dp).noRippleClickable { onColumnClick(ResultSortColumn.PUBLIC) },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Text(
            "$total",
            modifier = Modifier.width(60.dp).noRippleClickable { onColumnClick(ResultSortColumn.TOTAL) },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontWeight = FontWeight.ExtraBold
        )
    }
}

// ── Rank shift slope chart ────────────────────────────────────────────────────

private val RankShiftRowHeight = 24.dp
private val RankShiftColumnWidth = 48.dp
private const val RankShiftHighlightTop = 10

@Composable
private fun RankShiftCard(data: AfterShowData, animVersion: Int = -1, sideInset: Dp = 16.dp) {
    val s = LocalAppStrings.current
    InfographicSection(s.aftershowRankShift, GlowGold, sideInset = sideInset) {
        if (!data.hasVotes) {
            Text(s.aftershowNoVotes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            val progress = remember(animVersion) { Animatable(if (animVersion == -1) 1f else 0f) }
            LaunchedEffect(animVersion) {
                if (animVersion > 0 && animVersion % 2 == 1) progress.animateTo(1f, tween(1100, easing = FastOutSlowInEasing))
            }
            // The group's top 10 is highlighted; the rest are dimmed so the chart stays readable.
            val groupByRank    = data.userRanks.entries.associate { (order, rank) -> rank to order }
            val officialByRank = data.officialRanks.entries.associate { (order, rank) -> rank to order }
            val rowCount = maxOf(groupByRank.keys.maxOrNull() ?: 0, officialByRank.keys.maxOrNull() ?: 0)
            val highlighted: (Int) -> Boolean = { order -> (data.userRanks[order] ?: Int.MAX_VALUE) <= RankShiftHighlightTop }
            val labelAlpha: (Int) -> Float = { order -> if (highlighted(order)) 1f else 0.35f }
            val flagOf: (Int) -> String = { order -> data.orderToParticipant[order]?.let { countryFlag(it.country) } ?: "🏳️" }

            Row(Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                Text(
                    s.aftershowColGroup.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = GlowOrange,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    s.aftershowColOfficial.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = GlowGreen,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
            val dimColor = MaterialTheme.colorScheme.onSurfaceVariant
            Row(Modifier.fillMaxWidth().height(RankShiftRowHeight * rowCount)) {
                RankShiftColumn(rowCount, groupByRank, GlowOrange, leading = true, labelAlpha, flagOf)
                Canvas(Modifier.weight(1f).fillMaxHeight().padding(horizontal = 6.dp)) {
                    val rowPx = RankShiftRowHeight.toPx()
                    val dotRadius = 2.5.dp.toPx()
                    // Dim lines first, so the highlighted ones are drawn on top.
                    val orders = data.userRanks.keys.filter { it in data.officialRanks }.sortedBy { highlighted(it) }
                    for (order in orders) {
                        val start = Offset(0f, (data.userRanks.getValue(order) - 0.5f) * rowPx)
                        val end   = Offset(size.width, (data.officialRanks.getValue(order) - 0.5f) * rowPx)
                        val tip   = start + (end - start) * progress.value
                        if (highlighted(order)) {
                            drawLine(
                                Brush.linearGradient(listOf(GlowOrange, GlowGreen), start, end),
                                start, tip, strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round
                            )
                            drawCircle(GlowOrange, dotRadius, start)
                            drawCircle(GlowGreen.copy(alpha = progress.value), dotRadius, end)
                        } else {
                            drawLine(dimColor.copy(alpha = 0.2f), start, tip, strokeWidth = 1.dp.toPx(), cap = StrokeCap.Round)
                        }
                    }
                }
                RankShiftColumn(rowCount, officialByRank, GlowGreen, leading = false, labelAlpha, flagOf)
            }
        }
    }
}

@Composable
private fun RankShiftColumn(
    rowCount: Int,
    orderByRank: Map<Int, Int>,
    rankColor: Color,
    leading: Boolean,
    alphaOf: (Int) -> Float,
    flagOf: (Int) -> String
) {
    Column(Modifier.width(RankShiftColumnWidth.fontScaled())) {
        for (rank in 1..rowCount) {
            val order = orderByRank[rank]
            Row(
                Modifier.height(RankShiftRowHeight).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (leading) Arrangement.End else Arrangement.Start
            ) {
                if (order != null) {
                    val rankText = @Composable {
                        Text(
                            "$rank",
                            style = MaterialTheme.typography.labelMedium,
                            color = rankColor,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = if (leading) TextAlign.End else TextAlign.Start,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.width(20.dp.fontScaled())
                        )
                    }
                    val flag = @Composable { Text(flagOf(order), fontSize = 15.sp, lineHeight = 18.sp) }
                    Row(Modifier.alpha(alphaOf(order)), verticalAlignment = Alignment.CenterVertically) {
                        if (leading) { rankText(); Spacer(Modifier.width(4.dp)); flag() }
                        else         { flag(); Spacer(Modifier.width(4.dp)); rankText() }
                    }
                }
            }
        }
    }
}

@Composable
private fun RankBadge(value: String, color: Color, width: Dp = 50.dp) {
    Box(
        Modifier
            .width(width)
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(6.dp))
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(value, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold)
    }
}
