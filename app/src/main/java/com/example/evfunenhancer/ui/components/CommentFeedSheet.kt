package com.example.evfunenhancer.ui.components

import android.graphics.BlurMaskFilter
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import com.example.evfunenhancer.ui.theme.GradientPink
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evfunenhancer.ui.strings.LocalAppStrings
import com.example.evfunenhancer.ui.theme.CommentAccent
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Height of the collapsed handle; callers pad their content by this so nothing hides under it.
val CommentFeedPeekHeight = 48.dp

enum class CommentFeedTab { BY_COUNTRY, LATEST }

// One comment as shown in the feed. seenAt is when this device first saw it arrive
// (null = it was already there when the app started / the show was selected).
data class FeedComment(
    val order: Int,
    val flag: String,
    val countryName: String,
    val uid: String,
    val username: String,
    val color: Color,
    val points: Int?,
    val text: String,
    val seenAt: Long?,
    val isNew: Boolean
)

private const val EXPANDED_FRACTION = 0.85f
private const val FLING_VELOCITY = 600f
private const val PREVIEW_SLIDE_MS = 350
private val PillShape = RoundedCornerShape(50)
private val GlowPink = Color(0xFFEC4899)

// Bottom sheet of all comments for the selected show, anchored to the bottom of its parent
// (the Points screen, which already sits above the nav bar). Collapsed it is just a handle;
// swiping the handle up or tapping it expands the sheet. Read-only.
@Composable
fun CommentFeedSheet(
    comments: List<FeedComment>,
    unreadCount: Int,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    tab: CommentFeedTab,
    onTabChange: (CommentFeedTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(if (expanded) 1f else 0f) }
    LaunchedEffect(expanded) { progress.animateTo(if (expanded) 1f else 0f, tween(250)) }

    BackHandler(enabled = expanded) { onExpandedChange(false) }

    // Shared by the tab pill (whose highlight tracks the swipe position) and the pages.
    val tabs = CommentFeedTab.entries
    val pagerState = rememberPagerState(initialPage = tabs.indexOf(tab), pageCount = { tabs.size })
    // External tab changes (e.g. a toast opening the Latest tab) -> pager
    LaunchedEffect(tab) {
        val target = tabs.indexOf(tab)
        if (pagerState.currentPage != target) pagerState.animateScrollToPage(target)
    }
    // Pager (swipe or pill tap) -> tab state
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { onTabChange(tabs[it]) }
    }

    // Clipped so the collapsed sheet body (offset below the handle) never draws outside.
    BoxWithConstraints(modifier.fillMaxSize().clipToBounds()) {
        val density = LocalDensity.current
        val sheetHeight = maxHeight * EXPANDED_FRACTION
        val travelPx = with(density) { (sheetHeight - CommentFeedPeekHeight).toPx() }
        val p = progress.value

        // Scrim over the table; only intercepts touches while the sheet is (partly) open.
        if (p > 0.01f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f * p))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onExpandedChange(false) }
            )
        }

        fun dragBy(deltaY: Float) {
            scope.launch { progress.snapTo((progress.value - deltaY / travelPx).coerceIn(0f, 1f)) }
        }

        // Snaps to open/closed after a drag, from the fling velocity or else the nearest end.
        suspend fun settle(velocityY: Float) {
            val target = when {
                velocityY < -FLING_VELOCITY -> true
                velocityY > FLING_VELOCITY -> false
                else -> progress.value > 0.5f
            }
            if (target == expanded) {
                progress.animateTo(if (target) 1f else 0f, tween(200))
            } else {
                onExpandedChange(target)
            }
        }

        val dragState = rememberDraggableState { delta -> dragBy(delta) }

        // Lets a downward swipe anywhere in the list pull the sheet closed once the list is
        // scrolled to the top (whatever the list can't consume moves the sheet instead).
        // Only direct touch input counts, so a fling that hits the top doesn't close it.
        val listNestedScroll = object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Sheet already pulled down partway: dragging back up restores it first.
                if (source == NestedScrollSource.UserInput && available.y < 0f && progress.value < 1f) {
                    dragBy(available.y)
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && available.y > 0f) {
                    dragBy(available.y)
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (progress.value < 1f) {
                    settle(available.y)
                    return available
                }
                return Velocity.Zero
            }
        }

        Surface(
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 8.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(sheetHeight)
                .offset { IntOffset(0, (travelPx * (1f - p)).roundToInt()) }
        ) {
            Column {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(CommentFeedPeekHeight)
                        .draggable(
                            state = dragState,
                            orientation = Orientation.Vertical,
                            onDragStopped = { velocity -> settle(velocity) }
                        )
                        .clickable { onExpandedChange(!expanded) }
                ) {
                    Box(
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 6.dp)
                            .size(width = 34.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outline)
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                    ) {
                        if (p < 0.5f) {
                            CollapsedHandleRow(comments, unreadCount, Modifier.alpha(1f - p * 2f))
                        } else {
                            ExpandedHeaderRow(
                                pagerState = pagerState,
                                onTabClick = { t -> scope.launch { pagerState.animateScrollToPage(tabs.indexOf(t)) } },
                                // Nudged down for a little more room below the grab bar. Measured at
                                // its natural height, so a large system font lets it reach into the
                                // gap below instead of clipping the tab labels.
                                modifier = Modifier
                                    .wrapContentHeight(Alignment.Top, unbounded = true)
                                    .offset(y = 4.dp)
                                    .alpha(p * 2f - 1f)
                            )
                        }
                    }
                }
                // Fixed gap below the header, so comments don't scroll right up against it.
                Spacer(Modifier.height(12.dp))
                // Always composed (clipped away while collapsed) so the shared pager state can be
                // scrolled to the right tab before the sheet opens.
                FeedPages(comments, pagerState, Modifier.nestedScroll(listNestedScroll))
            }
        }
    }
}

@Composable
private fun CollapsedHandleRow(comments: List<FeedComment>, unreadCount: Int, modifier: Modifier) {
    val s = LocalAppStrings.current
    val newest = comments.filter { it.seenAt != null }.maxByOrNull { it.seenAt!! }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "💬 ${comments.size}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        // Slides in with the first unread comment's preview; once shown, later comments only
        // update its count. Disappears instantly so it never shows a zero count on the way out.
        AnimatedVisibility(
            visible = unreadCount > 0,
            enter = slideInVertically(tween(PREVIEW_SLIDE_MS)) { it } + fadeIn(tween(PREVIEW_SLIDE_MS)),
            exit = ExitTransition.None,
            modifier = Modifier.clipToBounds()
        ) {
            NewBadge(s.commentsNew(unreadCount))
        }
        // A newer comment pushes the previous preview up and out while it slides in from below.
        AnimatedContent(
            targetState = newest,
            contentKey = { it?.let { c -> "${c.order}_${c.uid}_${c.seenAt}" } },
            transitionSpec = {
                (slideInVertically(tween(PREVIEW_SLIDE_MS)) { it } + fadeIn(tween(PREVIEW_SLIDE_MS)))
                    .togetherWith(slideOutVertically(tween(PREVIEW_SLIDE_MS)) { -it } + fadeOut(tween(PREVIEW_SLIDE_MS)))
            },
            modifier = Modifier
                .weight(1f)
                .clipToBounds(),
            label = "handlePreview"
        ) { c ->
            Text(
                text = c?.let { "${s.commentByOn(it.username, it.flag)}: ${it.text}" } ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Text("⌃", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
    }
}

@Composable
private fun ExpandedHeaderRow(
    pagerState: PagerState,
    onTabClick: (CommentFeedTab) -> Unit,
    modifier: Modifier
) {
    val s = LocalAppStrings.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "💬 ${s.commentsFeedTitle}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        // Same look as the language toggle on the Profile tab, just smaller. The gradient
        // highlight is drawn behind the labels and follows the pager's live swipe position,
        // interpolating between the two segments' (differently sized) bounds.
        val surfaceColor = MaterialTheme.colorScheme.surface
        val outlineColor = MaterialTheme.colorScheme.outline
        val segmentBounds = remember { mutableStateMapOf<Int, Pair<Float, Float>>() } // x, width
        val selectedIndex by remember {
            derivedStateOf { (pagerState.currentPage + pagerState.currentPageOffsetFraction).roundToInt() }
        }
        Row(
            modifier = Modifier.drawBehind {
                val cr = CornerRadius(size.height / 2)
                drawRoundRect(color = surfaceColor, cornerRadius = cr)
                drawRoundRect(color = outlineColor, cornerRadius = cr, style = Stroke(width = 1.dp.toPx()))
                val first = segmentBounds[0] ?: return@drawBehind
                val second = segmentBounds[1] ?: return@drawBehind
                val t = (pagerState.currentPage + pagerState.currentPageOffsetFraction).coerceIn(0f, 1f)
                val x = first.first + (second.first - first.first) * t
                val w = first.second + (second.second - first.second) * t
                drawIntoCanvas { canvas ->
                    val paint = Paint()
                    paint.asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = GlowPink.copy(alpha = 0.8f).toArgb()
                        maskFilter = BlurMaskFilter(10.dp.toPx(), BlurMaskFilter.Blur.OUTER)
                    }
                    canvas.drawRoundRect(x, 0f, x + w, size.height, cr.x, cr.y, paint)
                }
                drawRoundRect(
                    brush = GradientPink,
                    topLeft = Offset(x, 0f),
                    size = Size(w, size.height),
                    cornerRadius = cr
                )
            }
        ) {
            listOf(
                CommentFeedTab.BY_COUNTRY to s.commentsFeedByCountry,
                CommentFeedTab.LATEST to s.commentsFeedLatest
            ).forEachIndexed { index, (t, label) ->
                val selected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .onPlaced { segmentBounds[index] = it.positionInParent().x to it.size.width.toFloat() }
                        .clip(PillShape)
                        .clickable { onTabClick(t) }
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// The two tabs as pages, so swiping left/right switches between them. Each page keeps its
// own scroll position.
@Composable
private fun FeedPages(
    comments: List<FeedComment>,
    pagerState: PagerState,
    listModifier: Modifier
) {
    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
        when (CommentFeedTab.entries[page]) {
            CommentFeedTab.BY_COUNTRY -> ByCountryList(comments, listModifier)
            CommentFeedTab.LATEST -> LatestList(comments, listModifier)
        }
    }
}

private val FeedListPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)

@Composable
private fun ByCountryList(comments: List<FeedComment>, modifier: Modifier) {
    // Latest-performing country first (reverse running order).
    val groups = comments.groupBy { it.order }.toSortedMap(compareByDescending { it })

    // New comments are inserted wherever their country sits, and animateItem slides the items
    // below them down. The one case that needs help is a new country group above the first
    // header while the list is at the top: LazyColumn would keep the old header anchored and
    // the new group would land just out of view, so re-pin to the top in the same frame (as in
    // LatestList).
    val listState = rememberLazyListState()
    val firstKey = groups.keys.firstOrNull()
    var lastFirstKey by remember { mutableStateOf(firstKey) }
    if (firstKey != lastFirstKey) {
        if (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
            listState.requestScrollToItem(0, 0)
        }
        lastFirstKey = firstKey
    }

    LazyColumn(state = listState, contentPadding = FeedListPadding, modifier = modifier.fillMaxSize()) {
        groups.forEach { (order, group) ->
            val first = group.first()
            // The list already sits a gap below the sheet header, so the top group needs no more.
            val headerTopPadding = if (order == firstKey) 0.dp else 10.dp
            item(key = "h_$order") {
                Text(
                    "$order ${first.flag} ${first.countryName}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .animateItem(
                            fadeInSpec = tween(NEW_COMMENT_ANIM_MS),
                            placementSpec = tween(NEW_COMMENT_ANIM_MS),
                            fadeOutSpec = null
                        )
                        .padding(top = headerTopPadding, bottom = 2.dp)
                )
            }
            items(group.sortedBy { it.username }, key = { "c_${it.order}_${it.uid}" }) {
                FeedItem(
                    it,
                    meta = null,
                    modifier = Modifier.animateItem(
                        fadeInSpec = tween(NEW_COMMENT_ANIM_MS),
                        placementSpec = tween(NEW_COMMENT_ANIM_MS),
                        fadeOutSpec = null
                    )
                )
            }
        }
    }
}

private const val NEW_COMMENT_ANIM_MS = 400

@Composable
private fun LatestList(comments: List<FeedComment>, modifier: Modifier) {
    val s = LocalAppStrings.current

    // Only comments seen arriving while the app was open have a time to sort by; older ones
    // are left to the Running order tab.
    val sortedLive = comments.filter { it.seenAt != null }.sortedByDescending { it.seenAt }

    // LazyColumn keeps the current first item anchored by key when something is inserted
    // above it, so nothing on screen would move and a new comment would arrive unnoticed.
    // Re-pinning the scroll position by index in the same frame the new comment arrives makes
    // the visible items slide down one slot (animateItem) wherever the user is in the list;
    // at the top, that reveals the new comment itself.
    val listState = rememberLazyListState()
    val newestKey = sortedLive.firstOrNull()?.let { "l_${it.order}_${it.uid}_${it.seenAt}" }
    var lastNewestKey by remember { mutableStateOf(newestKey) }
    if (newestKey != lastNewestKey) {
        listState.requestScrollToItem(
            listState.firstVisibleItemIndex,
            listState.firstVisibleItemScrollOffset
        )
        lastNewestKey = newestKey
    }

    LazyColumn(state = listState, contentPadding = FeedListPadding, modifier = modifier.fillMaxSize()) {
        items(sortedLive, key = { "l_${it.order}_${it.uid}" }) {
            FeedItem(
                it,
                meta = "${it.flag} ${it.countryName}",
                modifier = Modifier.animateItem(
                    fadeInSpec = tween(NEW_COMMENT_ANIM_MS),
                    placementSpec = tween(NEW_COMMENT_ANIM_MS),
                    fadeOutSpec = null
                )
            )
        }
        if (sortedLive.isEmpty()) {
            item(key = "empty") {
                Text(
                    s.commentsFeedEmpty,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun FeedItem(c: FeedComment, meta: String?, modifier: Modifier = Modifier) {
    val s = LocalAppStrings.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        CommentAvatar(c.username, c.color)
        CommentPointsChip(c.points, Modifier.padding(top = 3.dp))
        Column(Modifier.padding(top = 2.dp)) {
            Text(
                buildAnnotatedString {
                    append(c.text)
                    if (c.isNew) {
                        append("  ")
                        withStyle(
                            SpanStyle(
                                background = CommentAccent,
                                color = Color(0xFF04232A),
                                fontWeight = FontWeight.ExtraBold,
                                fontStyle = FontStyle.Normal,
                                fontSize = 9.sp
                            )
                        // Non-breaking padding: a plain space here would be where the line
                        // breaks, dropping the tag's left padding when it wraps.
                        ) { append(" ${s.commentNewTag} ") }
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (meta != null) {
                Text(
                    meta,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun NewBadge(text: String) {
    Text(
        text,
        fontSize = 9.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF04232A),
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(CommentAccent)
            .padding(horizontal = 5.dp, vertical = 1.dp)
    )
}
