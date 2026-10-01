package com.example.evfunenhancer.ui.components

import android.view.Gravity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import com.example.evfunenhancer.ui.strings.LocalAppStrings
import kotlinx.coroutines.delay

@Composable
fun NumberPickerDialog(
    flag: String,
    countryName: String,
    artist: String,
    song: String,
    currentValue: Int,
    showWinnerGuess: Boolean,
    currentUserGuessRank: Int?,
    guessFlags: Map<Int, String>,
    onGuessChanged: (Int?) -> Unit,
    commentText: String?,
    onCommentClick: () -> Unit,
    otherComments: List<EntryComment>,
    unreadCommentUids: Set<String>,
    onConfirm: (points: Int, chipScreenCenter: Offset) -> Unit,
    onDismiss: () -> Unit
) {
    val chipPositions = remember { mutableStateMapOf<Int, Offset>() }
    val pinned = remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        // Must be inside Dialog so LocalView resolves to the dialog's own ComposeView,
        // whose parent is a DialogWindowProvider.
        val dialogView = LocalView.current
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 0.dp,
            modifier = Modifier.onSizeChanged { size ->
                if (!pinned.value && size.height > 0) {
                    dialogView.post {
                        val window = (dialogView.parent as? DialogWindowProvider)?.window ?: return@post
                        val decorView = window.decorView
                        val location = IntArray(2)
                        decorView.getLocationOnScreen(location)
                        // attrs.y with Gravity.TOP is relative to the visible display area
                        // (below status bar), so subtract the status bar top offset.
                        val displayFrame = android.graphics.Rect()
                        decorView.getWindowVisibleDisplayFrame(displayFrame)
                        window.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL)
                        val attrs = window.attributes
                        attrs.y = location[1] - displayFrame.top
                        window.attributes = attrs
                        pinned.value = true
                    }
                }
            }
        ) {
            Column(
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.width(IntrinsicSize.Min),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("$flag ", fontSize = 33.sp)
                        Text(
                            text = countryName,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                if (artist.isNotBlank() || song.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$artist - $song",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            softWrap = false,
                            modifier = Modifier
                                .weight(weight = 1f, fill = false)
                                .padding(start = 4.dp)
                                .basicMarquee(velocity = 60.dp)
                        )
                    }
                    Spacer(Modifier.height(15.dp))
                } else {
                    Spacer(Modifier.height(13.dp))
                }

                // Square chips that share the width, with the same gap horizontally and vertically.
                // The side inset keeps the chips a bit smaller than the full dialog width would make them.
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(ChipGap)
                ) {
                    listOf(1..4, 5..8, 9..12).forEach { range ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(ChipGap)
                        ) {
                            range.forEach { value ->
                                PointChip(
                                    value = value,
                                    isCurrent = value == currentValue,
                                    color = heatColor(value),
                                    onClick = { onConfirm(value, chipPositions[value] ?: Offset.Zero) },
                                    onScreenCenterPositioned = { offset -> chipPositions[value] = offset },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(Modifier.height(8.dp))
                Column(
                    modifier = Modifier.bleed(OptionRowBleed),
                    verticalArrangement = Arrangement.spacedBy(OptionRowGap)
                ) {
                    if (showWinnerGuess) {
                        GuessWinnerRow(
                            currentRank = currentUserGuessRank,
                            guessFlags = guessFlags,
                            onGuessChanged = onGuessChanged
                        )
                    }
                    OwnCommentRow(text = commentText, onClick = onCommentClick)
                    CommentCarousel(
                        comments = otherComments,
                        unreadUids = unreadCommentUids,
                        modifier = Modifier.optionRow(background = Color.White.copy(alpha = 0.06f))
                    )
                }
            }
        }
    }
}

private val ChipGap = 10.dp

// The rows below the point chips (guess winner, own comment, others' comments) share one layout:
// a fixed-height row with an icon on the left, the text in the middle and a small badge (medal,
// pencil, points) on the right. The whole row is the tap target. The rows reach further out than
// the chips, into the dialog's side padding, to give the text more room.
private val OptionRowHeight = 34.dp
private val OptionRowGap = 0.dp
private val OptionRowBleed = 12.dp
private val OptionRowInset = 12.dp
private val OptionRowShape = RoundedCornerShape(12.dp)
private val OptionLeadingSize = 24.dp
private val OptionTrailingSize = 26.dp

internal fun Modifier.optionRow(onClick: (() -> Unit)? = null, background: Color? = null): Modifier = this
    .fillMaxWidth()
    .height(OptionRowHeight)
    .clip(OptionRowShape)
    .then(if (background != null) Modifier.background(background) else Modifier)
    .then(if (onClick != null) Modifier.clickable(interactionSource = null, indication = null, onClick = onClick) else Modifier)
    .padding(horizontal = OptionRowInset)

// Widens the element by `x` on both sides beyond the width its parent offers.
private fun Modifier.bleed(x: Dp): Modifier = layout { measurable, constraints ->
    val px = x.roundToPx()
    val placeable = measurable.measure(
        constraints.copy(minWidth = constraints.minWidth + 2 * px, maxWidth = constraints.maxWidth + 2 * px)
    )
    layout(placeable.width - 2 * px, placeable.height) { placeable.place(-px, 0) }
}

@Composable
internal fun OptionRow(
    leading: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    // Without a trailing slot the text runs to the end of the row.
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(OptionLeadingSize), contentAlignment = Alignment.Center) { leading() }
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp, end = if (trailing != null) 8.dp else 0.dp),
            contentAlignment = Alignment.CenterStart,
            content = content
        )
        if (trailing != null) {
            Box(Modifier.widthIn(min = OptionTrailingSize), contentAlignment = Alignment.Center) { trailing() }
        }
    }
}

@Composable
private fun OptionIcon(emoji: String) {
    Text(emoji, fontSize = 16.sp)
}

@Composable
internal fun OptionText(text: String, muted: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
        color = if (muted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

private const val GuessCollapseMs = 240

// FastOutSlowIn played backwards, so the frames fold away the way they unfolded.
private val CollapseEasing = CubicBezierEasing(0.8f, 0f, 0.6f, 1f)

// A chosen place (null = remove the guess), shown on the row until the saved guess comes back.
private class Pick(val rank: Int?)

private class PendingGuess {
    var pick by mutableStateOf<Pick?>(null)
    var saved = false
}

// The tint matches the Summary podium metals.
private class Medal(val rank: Int, val emoji: String, val tint: Color)

private val Medals = listOf(
    Medal(1, "🥇", Color(0xFFFFD60A)),
    Medal(2, "🥈", Color(0xFFB8C0D0)),
    Medal(3, "🥉", Color(0xFFC0673A)),
)

// "Guess winner" row with the chosen medal on the right. Tapping the row slides three podium
// tiles out to the left from the right end, covering the label; each shows the user's current
// pick for that place ("?" when empty). Tapping a tile puts this country there (replacing
// whoever held it) and collapses the row; tapping the tile this country already holds removes
// the guess. Tapping the row outside the tiles just collapses it.
@Composable
private fun GuessWinnerRow(
    currentRank: Int?,
    guessFlags: Map<Int, String>,
    onGuessChanged: (Int?) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    // A pick is saved only once the frames have collapsed: saving recomposes the whole points
    // table, which would otherwise swallow the collapse animation. The row shows the pick until
    // the saved guess comes back, so the medal doesn't blink off in between. If the dialog
    // closes first, the pick is saved on the way out.
    val pending = remember { PendingGuess() }
    LaunchedEffect(pending.pick) {
        val pick = pending.pick ?: return@LaunchedEffect
        delay(GuessCollapseMs.toLong())
        pending.saved = true
        onGuessChanged(pick.rank)
    }
    LaunchedEffect(currentRank) {
        if (pending.saved && pending.pick?.rank == currentRank) pending.pick = null
    }
    val latestOnGuessChanged by rememberUpdatedState(onGuessChanged)
    DisposableEffect(Unit) {
        onDispose { pending.pick?.takeIf { !pending.saved }?.let { latestOnGuessChanged(it.rank) } }
    }
    val shownRank = pending.pick.let { if (it != null) it.rank else currentRank }
    // The label fades in step with the frames unfolding and folding away. The medal needs no
    // fade: the frames have an opaque backing, so they simply cover and uncover it.
    val labelAlpha by animateFloatAsState(
        targetValue = if (open) 0f else 1f,
        animationSpec = tween(GuessCollapseMs),
        label = "guessLabel"
    )
    val medal = Medals.firstOrNull { it.rank == shownRank }
    Box(
        modifier = Modifier.optionRow(onClick = { open = !open }),
        contentAlignment = Alignment.CenterEnd
    ) {
        OptionRow(
            leading = { OptionIcon("🏆") },
            trailing = {
                if (medal != null) Text(medal.emoji, fontSize = 16.sp)
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Box(Modifier.alpha(labelAlpha)) {
                OptionText(LocalAppStrings.current.guessTheWinner, muted = true)
            }
        }
        AnimatedVisibility(
            visible = open,
            enter = expandHorizontally(tween(GuessCollapseMs), expandFrom = Alignment.End) + fadeIn(tween(180)),
            exit = shrinkHorizontally(tween(GuessCollapseMs, easing = CollapseEasing), shrinkTowards = Alignment.End) +
                fadeOut(tween(100, delayMillis = GuessCollapseMs - 100))
        ) {
            // Opaque backing so the tiles cover the label and medal underneath.
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Medals.forEach { medal ->
                    GuessPlace(
                        medal = medal,
                        occupant = guessFlags[medal.rank],
                        isThis = medal.rank == currentRank,
                        onClick = {
                            pending.saved = false
                            pending.pick = Pick(if (medal.rank == currentRank) null else medal.rank)
                            open = false
                        }
                    )
                }
            }
        }
    }
}

// One place in the guess row: a flat tile, shaped like the point chips, with the rank's medal
// and the flag of the country guessed there. The place this country holds is tinted its metal.
@Composable
private fun GuessPlace(medal: Medal, occupant: String?, isThis: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 54.dp, height = 30.dp)
            .clip(MaterialTheme.shapes.small)
            .background(
                if (isThis) medal.tint.copy(alpha = 0.30f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(medal.emoji, fontSize = 13.sp)
            if (occupant != null) {
                Text(occupant, fontSize = 17.sp)
            } else {
                Text(
                    "?",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                )
            }
        }
    }
}

// The user's own comment (one line), or a "Leave a comment" placeholder until they write one.
@Composable
private fun OwnCommentRow(text: String?, onClick: () -> Unit) {
    val hasText = !text.isNullOrEmpty()
    OptionRow(
        leading = { OptionIcon("💬") },
        trailing = {
            if (hasText) {
                Icon(
                    Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        },
        modifier = Modifier.optionRow(onClick = onClick)
    ) {
        if (hasText) OptionText(text!!, muted = true) else OptionText(LocalAppStrings.current.leaveComment, muted = true)
    }
}

internal fun heatColor(points: Int): Color {
    val t = (points - 1) / 11f
    return lerp(Color(0xFF3D68B8), Color(0xFFFF9100), t)
}

@Composable
private fun PointChip(
    value: Int,
    isCurrent: Boolean,
    color: Color,
    onClick: () -> Unit,
    onScreenCenterPositioned: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = color,
        contentColor = Color.White,
        border = if (isCurrent) BorderStroke(2.5.dp, Color.White) else null,
        modifier = modifier
            .aspectRatio(1f)
            .onGloballyPositioned { coords ->
                val posInWindow = coords.positionInWindow()
                val windowPos = IntArray(2)
                view.getLocationOnScreen(windowPos)
                onScreenCenterPositioned(
                    Offset(
                        posInWindow.x + windowPos[0] + coords.size.width / 2f,
                        posInWindow.y + windowPos[1] + coords.size.height / 2f
                    )
                )
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
