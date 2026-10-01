package com.example.evfunenhancer.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evfunenhancer.data.Participant
import com.example.evfunenhancer.ui.components.CommentEntryDialog
import com.example.evfunenhancer.ui.components.CommentFeedPeekHeight
import com.example.evfunenhancer.ui.components.CommentFeedSheet
import com.example.evfunenhancer.ui.components.CommentFeedTab
import com.example.evfunenhancer.ui.components.EntryComment
import com.example.evfunenhancer.ui.components.FeedComment
import com.example.evfunenhancer.ui.components.memberColors
import com.example.evfunenhancer.ui.components.ConfettiOverlay
import com.example.evfunenhancer.ui.components.NumberPickerDialog
import com.example.evfunenhancer.ui.strings.LocalAppStrings
import com.example.evfunenhancer.ui.glow
import com.example.evfunenhancer.ui.theme.CommentAccent
import com.example.evfunenhancer.utils.countryFlag
import com.example.evfunenhancer.viewmodel.MainViewModel
import kotlin.math.roundToInt

private val RANK_COL = 28.dp
private val FLAG_COL = 44.dp
private val SCORE_COL = 52.dp

private val UserHighlight = Color(0xFF9666ff)
private val RankShadeColor = Color(0x99000000)

private data class FlyingVote(val points: Int, val start: Offset, val target: Offset)

// Pulls the active user's column out of the list so it can be rendered pinned,
// outside the horizontally scrollable member columns.
private fun splitOwnMember(
    sortedMembers: List<Pair<String, String>>,
    activeUid: String?
): Pair<Pair<String, String>?, List<Pair<String, String>>> {
    val own = sortedMembers.firstOrNull { it.first == activeUid }
    val rest = if (own != null) sortedMembers.filter { it.first != activeUid } else sortedMembers
    return own to rest
}

@Composable
fun PointsScreen(vm: MainViewModel = viewModel()) {
    val shows by vm.shows.collectAsState()
    val selectedShowId by vm.selectedShowId.collectAsState()
    val votes by vm.votes.collectAsState()
    val members by vm.members.collectAsState()
    val activeUser by vm.username.collectAsState()
    val myUid = vm.myUid
    val guesses by vm.guesses.collectAsState()
    val comments by vm.comments.collectAsState()
    val commentSeenAt by vm.commentSeenAt.collectAsState()
    val unreadComments by vm.unreadComments.collectAsState()

    val participants = shows[selectedShowId] ?: emptyList()
    // sortedMembers: list of (uid, displayName) sorted by display name
    val sortedMembers = remember(members, myUid) {
        members.entries.sortedWith(compareBy({ it.key != myUid }, { it.value })).map { it.key to it.value }
    }
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
    // Other members' comments on the dialog's entry that were unread when it opened; the
    // carousel shows these first. Captured on open because opening marks them read.
    var dialogUnreadUids by remember { mutableStateOf(emptySet<String>()) }
    var showCommentEntry by remember { mutableStateOf(false) }
    var feedExpanded by remember { mutableStateOf(false) }
    var feedTab by remember { mutableStateOf(CommentFeedTab.BY_COUNTRY) }
    // Comments tagged NEW in the feed: whatever was unread when it opened, plus anything
    // arriving while it stays open. Everything is marked read as soon as it's on screen.
    var feedNewKeys by remember { mutableStateOf(emptySet<MainViewModel.CommentKey>()) }
    val totalComments = comments.values.sumOf { it.size }
    val unreadCountByOrder = remember(unreadComments) { unreadComments.groupingBy { it.order }.eachCount() }
    val unreadOrders = unreadCountByOrder.keys
    val colorByUid = remember(members, myUid) { memberColors(members.keys, myUid) }
    var showConfetti by remember { mutableStateOf(false) }

    // Opening a vote dialog counts as reading that entry's comments. Ones arriving while it's
    // open stay unread until it's opened again.
    LaunchedEffect(dialogParticipant?.order) {
        val order = dialogParticipant?.order ?: return@LaunchedEffect
        if (order in unreadOrders) vm.markCommentsRead(order)
    }
    LaunchedEffect(feedExpanded, unreadComments) {
        if (!feedExpanded) {
            feedNewKeys = emptySet()
        } else if (unreadComments.isNotEmpty()) {
            feedNewKeys = feedNewKeys + unreadComments
            vm.markAllCommentsRead()
        }
    }
    // The handle disappears with the last comment, so don't leave the sheet open behind it.
    LaunchedEffect(totalComments) {
        if (totalComments == 0) feedExpanded = false
    }

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
            PointsBanner(
                title = s.votingOn,
                showLabel = s.showTitle(selectedShowId ?: ""),
                scored = participants.count { votes[it.order]?.containsKey(myUid) == true },
                total = participants.size
            )
            HeaderRow(sortedMembers, myUid, scrollState)
            HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.outlineVariant)
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = if (totalComments > 0) CommentFeedPeekHeight else 0.dp)
            ) {
                itemsIndexed(participants, key = { _, p -> p.order }) { index, participant ->
                    ParticipantRow(
                        participant = participant,
                        sortedMembers = sortedMembers,
                        activeUid = myUid,
                        votes = votes[participant.order] ?: emptyMap(),
                        unreadCommentCount = unreadCountByOrder[participant.order] ?: 0,
                        guessLookup = guessLookup,
                        scrollState = scrollState,
                        onClick = {
                            dialogUnreadUids = unreadComments
                                .filter { it.order == participant.order }
                                .map { it.uid }
                                .toSet()
                            dialogParticipant = participant
                        },
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
        if (totalComments > 0) {
            val feedComments = remember(comments, commentSeenAt, feedNewKeys, unreadComments, votes, members, colorByUid, participants, s) {
                comments.flatMap { (order, byUid) ->
                    val participant = participants.firstOrNull { it.order == order } ?: return@flatMap emptyList()
                    byUid.map { (uid, text) ->
                        val key = MainViewModel.CommentKey(order, uid)
                        FeedComment(
                            order = order,
                            flag = countryFlag(participant.country),
                            countryName = s.translateCountry(participant.country),
                            uid = uid,
                            username = members[uid] ?: "?",
                            color = colorByUid[uid] ?: CommentAccent,
                            points = votes[order]?.get(uid),
                            text = text,
                            seenAt = commentSeenAt[key],
                            isNew = key in feedNewKeys || key in unreadComments
                        )
                    }
                }
            }
            CommentFeedSheet(
                comments = feedComments,
                unreadCount = unreadComments.size,
                expanded = feedExpanded,
                onExpandedChange = { feedExpanded = it },
                tab = feedTab,
                onTabChange = { feedTab = it }
            )
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

    dialogParticipant?.let { p -> key(p.order) {
        val current = votes[p.order]?.get(myUid) ?: 0
        val myComment = comments[p.order]?.get(myUid)
        val otherComments = comments[p.order].orEmpty()
            .filterKeys { it != myUid }
            .map { (uid, text) ->
                EntryComment(uid, members[uid] ?: "?", colorByUid[uid] ?: CommentAccent, votes[p.order]?.get(uid), text)
            }
        NumberPickerDialog(
            flag = countryFlag(p.country),
            countryName = s.translateCountry(p.country),
            artist = p.artist,
            song = p.song,
            currentValue = current,
            showWinnerGuess = selectedShowId == "final",
            currentUserGuessRank = guessLookup[myUid]?.get(p.order),
            guessFlags = guesses[myUid].orEmpty().mapNotNull { (rank, order) ->
                participants.find { it.order == order }?.let { rank to countryFlag(it.country) }
            }.toMap(),
            onGuessChanged = { rank -> vm.submitGuess(p.order, rank) },
            commentText = myComment,
            onCommentClick = { showCommentEntry = true },
            otherComments = otherComments,
            unreadCommentUids = dialogUnreadUids,
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
            onDismiss = {
                dialogParticipant = null
                showCommentEntry = false
            }
        )
        if (showCommentEntry) {
            CommentEntryDialog(
                countryName = s.translateCountry(p.country),
                initialText = myComment ?: "",
                onConfirm = { text ->
                    vm.submitComment(p.order, text)
                    showCommentEntry = false
                },
                onDismiss = { showCommentEntry = false }
            )
        }
    } }
}

// The app logo's "SCORE TRACKER" gradient (est_banner): cyan → blue → purple → pink.
private val BrandGradientColors = listOf(Color(0xFF1FD8F2), Color(0xFF5A82F6), Color(0xFFA855F7), Color(0xFFEC4899))

// Slim title strip in the style of the Aftershow lockup: "VOTING ON" in white spaced caps
// followed by the show's name in gradient caps, and a thin gradient line along the bottom
// showing how much of the show you've scored. Kept low so the table keeps its room.
@Composable
private fun PointsBanner(title: String, showLabel: String, scored: Int, total: Int) {
    val progress by animateFloatAsState(
        targetValue = if (total > 0) scored.toFloat() / total else 0f,
        animationSpec = tween(500),
        label = "bannerProgress"
    )
    val base = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
    Column(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF1E0A3E).copy(alpha = 0.5f), Color.Transparent))
            )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title.uppercase(),
                style = base.copy(letterSpacing = 0.3.em),
                color = Color.White
            )
            Text(
                " " + showLabel.uppercase(),
                style = base.copy(letterSpacing = 0.3.em, brush = Brush.horizontalGradient(BrandGradientColors))
            )
        }
        // Progress line: full-width gradient, revealed up to the scored fraction.
        Box(
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .drawBehind {
                    drawRect(Color.White.copy(alpha = 0.08f))
                    drawRect(
                        Brush.horizontalGradient(BrandGradientColors, endX = size.width),
                        size = size.copy(width = size.width * progress)
                    )
                }
        )
    }
}

@Composable
private fun HeaderRow(
    sortedMembers: List<Pair<String, String>>,
    activeUid: String?,
    scrollState: ScrollState
) {
    val (own, rest) = splitOwnMember(sortedMembers, activeUid)
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(Color(0xFF1A1833))
            .padding(start = 4.dp)
    ) {
        HeaderCell(RANK_COL + FLAG_COL, "#")
        own?.let { (_, name) ->
            HeaderCell(width = SCORE_COL, text = name.uppercase(), bold = true, highlight = true)
        }
        Row(Modifier.horizontalScroll(scrollState)) {
            rest.forEach { (uid, name) ->
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
    unreadCommentCount: Int,
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
    val (own, rest) = splitOwnMember(sortedMembers, activeUid)
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(rowBg)
            .drawBehind { drawRect(brush = rankGradient) }
            .clickable(onClick = onClick)
            .padding(start = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DataCell(RANK_COL, "${participant.order}", alignEnd = true, bold = true)
        FlagCell(countryFlag(participant.country), unreadCommentCount)
        own?.let { (uid, _) ->
            key(uid) {
                val score = votes[uid]
                val medalRank = guessLookup[uid]?.get(participant.order)
                Box(
                    if (trackPosition) {
                        Modifier.onGloballyPositioned { coords ->
                            val posInWindow = coords.positionInWindow()
                            view.getLocationOnScreen(windowPos)
                            onActiveCellPositioned(
                                Offset(
                                    posInWindow.x + windowPos[0] + coords.size.width / 2f,
                                    posInWindow.y + windowPos[1] + coords.size.height / 2f
                                )
                            )
                        }
                    } else Modifier
                ) {
                    ScoreCell(
                        SCORE_COL, score?.toString() ?: "—", highlight = true, medalRank = medalRank,
                        flashOnChange = false
                    )
                }
            }
        }
        Row(Modifier.horizontalScroll(scrollState)) {
            rest.forEach { (uid, _) ->
                key(uid) {
                    val score = votes[uid]
                    val medalRank = guessLookup[uid]?.get(participant.order)
                    ScoreCell(
                        SCORE_COL, score?.toString() ?: "—", highlight = false, medalRank = medalRank,
                        flashOnChange = flashEnabled
                    )
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
            style = if (highlight) MaterialTheme.typography.bodyMedium.copy(
                shadow = Shadow(color = UserHighlight, offset = Offset.Zero, blurRadius = 20f)
            ) else MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) UserHighlight
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
            color = if (highlight) UserHighlight
                    else MaterialTheme.colorScheme.onSurface
        )
    }
}

// Flag cell with a small glowing badge in the top-right corner counting other members'
// comments on this entry that haven't been read yet.
@Composable
private fun FlagCell(flag: String, unreadCount: Int) {
    Box(Modifier.width(FLAG_COL)) {
        DataCell(FLAG_COL, flag)
        if (unreadCount > 0) {
            Text(
                text = unreadCount.toString(),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF04232A),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 2.dp)
                    .glow(CommentAccent, radius = 6.dp)
                    .background(CommentAccent, RoundedCornerShape(7.dp))
                    .padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun ScoreCell(
    width: Dp,
    score: String,
    highlight: Boolean,
    medalRank: Int?,
    flashOnChange: Boolean = false
) {
    val flashAlpha = remember { Animatable(0f) }
    var knownScore by remember { mutableStateOf(score) }
    LaunchedEffect(score) {
        val shouldFlash = flashOnChange && score != knownScore
        knownScore = score
        if (!shouldFlash) return@LaunchedEffect
        flashAlpha.snapTo(1f)
        flashAlpha.animateTo(0f, tween(1000))
    }

    val isEmpty = score == "—"
    // Outer box: fixed cell width/padding, matching RANK/FLAG cells exactly, so this cell's
    // content sits at the same vertical position as the rest of the row.
    Box(
        Modifier
            .width(width)
            .padding(horizontal = 4.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Inner box sized to just the score+medal group (not the full cell lane), so the
        // medal's offset is relative to this tight box and can't bleed into a neighboring
        // cell — that happened when the offset was relative to the full-width outer box.
        // Nested centering (inner box centered in outer, score centered in inner) still
        // lands the score at the same spot regardless of the inner box's own width, so this
        // doesn't reintroduce the "shifts when a medal is present" issue either.
        Box(contentAlignment = Alignment.Center) {
            Text(
                score,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (highlight && !isEmpty) FontWeight.Bold else FontWeight.Normal,
                color = if (isEmpty) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                        else if (highlight) UserHighlight
                        else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.drawBehind {
                    val fa = flashAlpha.value
                    if (fa > 0f) {
                        drawCircle(color = UserHighlight.copy(alpha = fa * 0.55f), radius = 17.dp.toPx())
                    }
                }
            )
            if (medalRank in 1..3) {
                Box(
                    Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = (-11).dp)
                        .size(7.dp)
                        .background(medalDotBrush(medalRank!!), CircleShape)
                )
            }
        }
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
