package com.example.evfunenhancer.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evfunenhancer.data.Participant
import com.example.evfunenhancer.ui.components.ConfettiOverlay
import com.example.evfunenhancer.ui.components.NumberPickerDialog
import com.example.evfunenhancer.ui.strings.LocalAppStrings
import com.example.evfunenhancer.utils.countryFlag
import com.example.evfunenhancer.viewmodel.MainViewModel
import kotlin.math.roundToInt

private val RANK_COL = 28.dp
private val FLAG_COL = 44.dp
private val SCORE_COL = 52.dp

private val GlowPurple = Color(0xFFA855F7)
private val ActiveTextShadow = Shadow(color = GlowPurple, offset = Offset.Zero, blurRadius = 20f)
private val RankShadeColor = Color(0x99000000)

private data class FlyingVote(val points: Int, val start: Offset, val target: Offset)

@Composable
fun PointsScreen(vm: MainViewModel = viewModel()) {
    val shows by vm.shows.collectAsState()
    val selectedShowId by vm.selectedShowId.collectAsState()
    val votes by vm.votes.collectAsState()
    val members by vm.members.collectAsState()
    val activeUser by vm.username.collectAsState()
    val myUid = vm.myUid
    val guesses by vm.guesses.collectAsState()

    val participants = shows[selectedShowId] ?: emptyList()
    // sortedMembers: list of (uid, displayName) sorted by display name
    val sortedMembers = remember(members) { members.entries.sortedBy { it.value }.map { it.key to it.value } }
    val scrollState = rememberScrollState()
    // guessLookup: uid → (participantOrder → rank)
    val guessLookup = remember(guesses) { guesses.mapValues { (_, rankToOrder) ->
        rankToOrder.entries.associate { (rank, order) -> order to rank }
    } }

    // True only after the first non-empty votes snapshot has been rendered.
    // Kept false during that composition so flash is suppressed for the initial load.
    var votesInitiallyLoaded by remember { mutableStateOf(false) }
    SideEffect {
        if (!votesInitiallyLoaded && votes.isNotEmpty()) votesInitiallyLoaded = true
    }

    val s = LocalAppStrings.current
    var dialogParticipant by remember { mutableStateOf<Participant?>(null) }
    var showConfetti by remember { mutableStateOf(false) }

    val view = LocalView.current
    val cellPositions = remember { mutableStateMapOf<Int, Offset>() }
    var flyingVote by remember { mutableStateOf<FlyingVote?>(null) }
    var rootScreenOrigin by remember { mutableStateOf(Offset.Zero) }

    val rootWindowPos = remember { IntArray(2) }
    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                val posInWindow = coords.positionInWindow()
                view.getLocationOnScreen(rootWindowPos)
                rootScreenOrigin = Offset(posInWindow.x + rootWindowPos[0], posInWindow.y + rootWindowPos[1])
            }
    ) {
        Column(Modifier.fillMaxSize()) {
            HeaderRow(sortedMembers, myUid, scrollState)
            HorizontalDivider()
            LazyColumn(Modifier.weight(1f)) {
                itemsIndexed(participants, key = { _, p -> p.order }) { index, participant ->
                    ParticipantRow(
                        participant = participant,
                        sortedMembers = sortedMembers,
                        activeUid = myUid,
                        votes = votes[participant.order] ?: emptyMap(),
                        guessLookup = guessLookup,
                        scrollState = scrollState,
                        onClick = { dialogParticipant = participant },
                        onActiveCellPositioned = { offset ->
                            cellPositions[participant.order] = offset
                        },
                        trackPosition = dialogParticipant?.order == participant.order,
                        isEvenRow = index % 2 == 0,
                        flashEnabled = votesInitiallyLoaded
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
        flyingVote?.let { fv ->
            FlyingVoteOverlay(
                points = fv.points,
                startOffset = fv.start,
                endOffset = fv.target,
                onFinished = { flyingVote = null }
            )
        }
        if (showConfetti) {
            ConfettiOverlay(onFinished = { showConfetti = false })
        }
    }

    dialogParticipant?.let { p ->
        val current = votes[p.order]?.get(myUid) ?: 0
        NumberPickerDialog(
            flag = countryFlag(p.country),
            countryName = s.translateCountry(p.country),
            currentValue = current,
            showWinnerGuess = selectedShowId == "final",
            currentUserGuessRank = guessLookup[myUid]?.get(p.order),
            onGuessChanged = { rank -> vm.submitGuess(p.order, rank) },
            onConfirm = { pts, chipScreenPos ->
                vm.submitVote(p.order, pts)
                val rawTarget = cellPositions[p.order]
                dialogParticipant = null
                if (rawTarget != null) {
                    val localStart = chipScreenPos - rootScreenOrigin
                    val localTarget = rawTarget - rootScreenOrigin
                    flyingVote = FlyingVote(pts, localStart, localTarget)
                }
                if (pts == 12) showConfetti = true
            },
            onDismiss = { dialogParticipant = null }
        )
    }
}

@Composable
private fun HeaderRow(
    sortedMembers: List<Pair<String, String>>,
    activeUid: String?,
    scrollState: ScrollState
) {
    val gradientEndX = with(LocalDensity.current) { (RANK_COL + FLAG_COL + SCORE_COL * 0.45f).toPx() }
    val rankGradient = remember(gradientEndX) {
        Brush.horizontalGradient(
            colors = listOf(RankShadeColor, Color.Transparent),
            startX = 0f,
            endX = gradientEndX
        )
    }
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .drawBehind { drawRect(brush = rankGradient) }
            .padding(start = 4.dp)
    ) {
        HeaderCell(RANK_COL + FLAG_COL, "#")
        Row(Modifier.horizontalScroll(scrollState)) {
            sortedMembers.forEach { (uid, name) ->
                HeaderCell(
                    width = SCORE_COL,
                    text = name.uppercase(),
                    bold = true,
                    highlight = uid == activeUid
                )
            }
        }
    }
}

@Composable
private fun ParticipantRow(
    participant: Participant,
    sortedMembers: List<Pair<String, String>>,
    activeUid: String?,
    votes: Map<String, Int>,
    guessLookup: Map<String, Map<Int, Int>>,
    scrollState: ScrollState,
    onClick: () -> Unit,
    onActiveCellPositioned: (Offset) -> Unit,
    trackPosition: Boolean = false,
    isEvenRow: Boolean = false,
    flashEnabled: Boolean = false
) {
    val view = LocalView.current
    val windowPos = remember { IntArray(2) }
    val gradientEndX = with(LocalDensity.current) { (RANK_COL + FLAG_COL + SCORE_COL * 0.45f).toPx() }
    val rankGradient = remember(gradientEndX) {
        Brush.horizontalGradient(
            colors = listOf(RankShadeColor, Color.Transparent),
            startX = 0f,
            endX = gradientEndX
        )
    }
    val rowBg = if (isEvenRow) MaterialTheme.colorScheme.background else Color(0xFF12102A)
    Row(
        Modifier
            .fillMaxWidth()
            .background(rowBg)
            .drawBehind { drawRect(brush = rankGradient) }
            .clickable(onClick = onClick)
            .padding(start = 4.dp)
    ) {
        DataCell(RANK_COL, "${participant.order}", alignEnd = true, bold = true)
        DataCell(FLAG_COL, countryFlag(participant.country))
        Row(Modifier.horizontalScroll(scrollState)) {
            sortedMembers.forEach { (uid, _) ->
                val score = votes[uid]
                val isActive = uid == activeUid
                val medalRank = guessLookup[uid]?.get(participant.order)
                if (isActive && trackPosition) {
                    Box(Modifier.onGloballyPositioned { coords ->
                        val posInWindow = coords.positionInWindow()
                        view.getLocationOnScreen(windowPos)
                        onActiveCellPositioned(
                            Offset(
                                posInWindow.x + windowPos[0] + coords.size.width / 2f,
                                posInWindow.y + windowPos[1] + coords.size.height / 2f
                            )
                        )
                    }) {
                        ScoreCell(SCORE_COL, score?.toString() ?: "—", highlight = true, medalRank = medalRank)
                    }
                } else {
                    ScoreCell(SCORE_COL, score?.toString() ?: "—", highlight = isActive, medalRank = medalRank, flashOnChange = if (isActive) false else flashEnabled)
                }
            }
        }
    }
}

@Composable
private fun HeaderCell(width: Dp, text: String, bold: Boolean = false, highlight: Boolean = false) {
    Box(
        Modifier
            .width(width)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = if (highlight) MaterialTheme.typography.bodyMedium.copy(shadow = ActiveTextShadow)
                    else MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DataCell(width: Dp, text: String, highlight: Boolean = false, alignEnd: Boolean = false, bold: Boolean = false) {
    Box(
        Modifier
            .width(width)
            .padding(
                start = if (alignEnd) 0.dp else 4.dp,
                end = if (alignEnd) 0.dp else 4.dp,
                top = 12.dp,
                bottom = 12.dp
            ),
        contentAlignment = if (alignEnd) Alignment.CenterEnd else Alignment.Center
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = when {
                bold -> FontWeight.SemiBold
                highlight -> FontWeight.Bold
                else -> FontWeight.Normal
            },
            color = if (highlight) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ScoreCell(width: Dp, score: String, highlight: Boolean, medalRank: Int?, flashOnChange: Boolean = false) {
    val ringColor = when (medalRank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFB0B8C0)
        3 -> Color(0xFFCD7F32)
        else -> null
    }
    val density = LocalDensity.current
    val medalStroke = remember(density) { Stroke(width = with(density) { 3.5.dp.toPx() }) }

    val flashAlpha = remember { Animatable(0f) }
    var knownScore by remember { mutableStateOf(score) }
    LaunchedEffect(score) {
        val shouldFlash = flashOnChange && score != knownScore
        knownScore = score
        if (!shouldFlash) return@LaunchedEffect
        flashAlpha.snapTo(1f)
        flashAlpha.animateTo(0f, tween(500))
    }

    Box(
        Modifier
            .width(width)
            .padding(horizontal = 4.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            score,
            style = if (highlight) MaterialTheme.typography.bodyMedium.copy(shadow = ActiveTextShadow)
                    else MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.drawBehind {
                val fa = flashAlpha.value
                if (fa > 0f) {
                    drawCircle(color = GlowPurple.copy(alpha = fa * 0.55f), radius = 17.dp.toPx())
                }
                if (ringColor != null) {
                    drawCircle(color = ringColor, radius = 14.dp.toPx(), style = medalStroke)
                }
            }
        )
    }
}

@Composable
private fun FlyingVoteOverlay(
    points: Int,
    startOffset: Offset,
    endOffset: Offset,
    onFinished: () -> Unit
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(startOffset, endOffset) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))
        onFinished()
    }

    val p = progress.value
    val currentX = lerp(startOffset.x, endOffset.x, p)
    val currentY = lerp(startOffset.y, endOffset.y, p)
    val scale = lerp(1.4f, 1f, p)
    val currentAlpha = if (p < 0.7f) 1f else 1f - (p - 0.7f) / 0.3f

    var textSize by remember { mutableStateOf(IntSize.Zero) }
    val color = MaterialTheme.colorScheme.primary

    Box(Modifier.fillMaxSize()) {
        Text(
            text = points.toString(),
            modifier = Modifier
                .onGloballyPositioned { textSize = it.size }
                .offset {
                    IntOffset(
                        (currentX - textSize.width / 2f).roundToInt(),
                        (currentY - textSize.height / 2f).roundToInt()
                    )
                }
                .scale(scale)
                .alpha(currentAlpha),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    start + fraction * (stop - start)
