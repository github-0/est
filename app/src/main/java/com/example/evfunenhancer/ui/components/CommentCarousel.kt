package com.example.evfunenhancer.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// One member's comment on an entry, with the points they gave it (null = not voted yet).
data class EntryComment(
    val uid: String,
    val username: String,
    val color: Color,
    val points: Int?,
    val text: String
)

private val OwnAccent = Color(0xFF9666FF)

// Distinct, readable-on-dark colours for other members; purple is reserved for "you".
private val MemberPalette = listOf(
    Color(0xFF06B6D4), // teal
    Color(0xFFF472B6), // pink
    Color(0xFFFBBF24), // amber
    Color(0xFF4ADE80), // green
    Color(0xFFF87171), // red
    Color(0xFF60A5FA), // blue
    Color(0xFFFB923C), // orange
    Color(0xFFA3E635), // lime
    Color(0xFFE879F9), // fuchsia
    Color(0xFF94A3B8), // slate
)

// Assigns each member a colour so comments are easy to tell apart at a glance. Purely
// client-side and not persisted: others are ordered by uid so the mapping stays stable while
// the member list is unchanged. Past the palette, hues are spread by the golden angle.
fun memberColors(uids: Collection<String>, myUid: String?): Map<String, Color> =
    uids.filter { it != myUid }.sorted().mapIndexed { i, uid ->
        uid to (MemberPalette.getOrNull(i)
            ?: Color.hsv((i * 137.508f) % 360f, 0.55f, 0.95f))
    }.toMap() + listOfNotNull(myUid?.let { it to OwnAccent })

private const val AUTO_ADVANCE_MS = 5000L
private const val SLIDE_MS = 350

// Single line of other members' comments on one entry, laid out like the dialog's other rows. Every 5 s the next comment slides up from below and pushes the
// current one out, like the new-comment preview on the collapsed comment feed. The order is
// shuffled once when the line first appears (so nobody's comment is always first), with unread
// comments shuffled in front of the rest, and then kept stable; comments that arrive while it
// is open are appended. Text that doesn't fit one line is ellipsized.
@Composable
fun CommentCarousel(
    comments: List<EntryComment>,
    unreadUids: Set<String>,
    modifier: Modifier = Modifier
) {
    if (comments.isEmpty()) return
    val byUid = comments.associateBy { it.uid }

    var order by remember {
        val (unread, read) = byUid.keys.partition { it in unreadUids }
        mutableStateOf(unread.shuffled() + read.shuffled())
    }
    val reconciled = order.filter { it in byUid } + (byUid.keys - order.toSet()).shuffled()
    if (reconciled != order) order = reconciled
    val items = reconciled.map { byUid.getValue(it) }

    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(items.size) {
        if (items.size < 2) return@LaunchedEffect
        while (true) {
            delay(AUTO_ADVANCE_MS)
            index = (index + 1) % items.size
        }
    }
    val current = items[index % items.size]

    AnimatedContent(
        targetState = current,
        contentKey = { it.uid },
        transitionSpec = {
            (slideInVertically(tween(SLIDE_MS)) { it } + fadeIn(tween(SLIDE_MS)))
                .togetherWith(slideOutVertically(tween(SLIDE_MS)) { -it } + fadeOut(tween(SLIDE_MS)))
        },
        modifier = modifier.clipToBounds(),
        label = "commentLine"
    ) { c ->
        // Same layout as the guess and own-comment rows above it in the picker dialog.
        OptionRow(
            leading = { CommentAvatar(c.username, c.color, muted = true) },
            modifier = Modifier.fillMaxSize()
        ) {
            OptionText(c.text)
        }
    }
}

@Composable
internal fun CommentAvatar(username: String, accent: Color, muted: Boolean = false) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = if (muted) 0.14f else 0.25f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            username.uppercase(),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (muted) accent.copy(alpha = 0.85f) else accent
        )
    }
}

// The points the commenter gave, heat-coloured like the picker chips; outlined dash if none.
// muted = translucent fill, so it doesn't compete with the real point buttons in the dialog.
@Composable
internal fun CommentPointsChip(points: Int?, modifier: Modifier = Modifier, muted: Boolean = false) {
    val shape = RoundedCornerShape(5.dp)
    Box(
        modifier = modifier
            .widthIn(min = 22.dp)
            .heightIn(min = 18.dp)
            .clip(shape)
            .then(
                if (points != null) Modifier.background(heatColor(points).copy(alpha = if (muted) 0.35f else 1f))
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outline, shape)
            )
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            points?.toString() ?: "–",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (points != null) Color.White.copy(alpha = if (muted) 0.85f else 1f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
