package com.example.evfunenhancer.ui.screens

import android.content.Intent
import android.graphics.BlurMaskFilter
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.BiasAlignment
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.foundation.Canvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import com.example.evfunenhancer.data.UpdateCheckResult
import com.example.evfunenhancer.data.UpdateInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evfunenhancer.R
import com.example.evfunenhancer.ui.components.SparkleOverlay
import com.example.evfunenhancer.ui.glow
import com.example.evfunenhancer.ui.strings.LocalAppStrings
import com.example.evfunenhancer.ui.theme.GradientPink
import com.example.evfunenhancer.viewmodel.MainViewModel
import kotlinx.coroutines.launch

private val SHOW_IDS = listOf("semi1", "semi2", "final")
private val PillShape = RoundedCornerShape(50)

private val GlowPink = Color(0xFFEC4899)
private val GlowPurple = Color(0xFFA855F7)
private val AccentBlue = Color(0xFF6d63fc)
private val NavButtonColor = Color(0xFFcf37ed)

private enum class NoRoomState { SELECTION, CREATING, JOINING }

private enum class ContentState(val level: Int) {
    SELECTION(0), CREATING(1), JOINING(1), JOINED(2)
}

@Composable
private fun GradientLangToggle(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    val langs = listOf("fi" to "FI", "en" to "EN")
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline
    Row(
        modifier = Modifier.drawBehind {
            val cr = CornerRadius(size.height / 2)
            drawRoundRect(color = surfaceColor, cornerRadius = cr)
            drawRoundRect(color = outlineColor, cornerRadius = cr, style = Stroke(width = 1.dp.toPx()))
        }
    ) {
        langs.forEach { (code, label) ->
            val selected = code == currentLanguage
            Box(
                modifier = Modifier
                    .then(
                        if (selected) Modifier.glow(GlowPink, radius = 16.dp, cornerRadius = 100.dp, alpha = 0.8f)
                        else Modifier
                    )
                    .clip(PillShape)
                    .then(
                        if (selected) Modifier.background(brush = GradientPink, shape = PillShape)
                        else Modifier
                    )
                    .clickable { onLanguageChange(code) }
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ShowSegmentedControl(
    shows: Map<String, List<*>>,
    pickedShowId: String?,
    showLabel: (String) -> String,
    onPick: (String) -> Unit
) {
    val selectedIndex = SHOW_IDS.indexOf(pickedShowId)
    // -1 encodes "hidden"; visibility is derived from the value itself so there is never
    // a frame where the indicator is visible at position 0 before snapping to the right place.
    val animatedFraction = remember { Animatable(selectedIndex.toFloat()) }
    LaunchedEffect(pickedShowId) {
        if (pickedShowId == null) {
            animatedFraction.snapTo(-1f)
        } else if (animatedFraction.value < 0f) {
            animatedFraction.snapTo(selectedIndex.toFloat())
        } else {
            animatedFraction.animateTo(selectedIndex.toFloat(), tween(200))
        }
    }
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .drawBehind {
                val cr = CornerRadius(size.height / 2)
                drawRoundRect(color = surfaceVariant, cornerRadius = cr)
                if (animatedFraction.value >= 0f) {
                    val segW = size.width / SHOW_IDS.size
                    val x = segW * animatedFraction.value
                    drawIntoCanvas { canvas ->
                        val p = Paint()
                        p.asFrameworkPaint().apply {
                            isAntiAlias = true
                            color = GlowPink.copy(alpha = 0.8f).toArgb()
                            maskFilter = BlurMaskFilter(16.dp.toPx(), BlurMaskFilter.Blur.OUTER)
                        }
                        canvas.drawRoundRect(x, 0f, x + segW, size.height, cr.x, cr.y, p)
                    }
                    drawRoundRect(
                        brush = GradientPink,
                        topLeft = Offset(x, 0f),
                        size = Size(segW, size.height),
                        cornerRadius = cr
                    )
                }
            }
    ) {
        SHOW_IDS.forEachIndexed { index, id ->
            val hasParticipants = shows[id]?.isNotEmpty() == true
            val selected = pickedShowId == id
            val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
            val onSurfaceDisabled = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            val textColor by animateColorAsState(
                targetValue = when {
                    selected -> Color.White
                    !hasParticipants -> onSurfaceDisabled
                    else -> onSurfaceVariantColor
                },
                animationSpec = tween(200),
                label = "segmentText_$id"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(if (hasParticipants) Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onPick(id) } else Modifier)
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = showLabel(id),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun GradientSaveButton(
    enabled: Boolean,
    isLoading: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (enabled) Modifier.glow(GlowPink, radius = 22.dp, cornerRadius = 100.dp, alpha = 0.5f)
                else Modifier
            )
            .clip(PillShape)
            .then(
                if (enabled) Modifier.background(brush = GradientPink, shape = PillShape)
                else Modifier.background(MaterialTheme.colorScheme.surfaceVariant, PillShape)
            )
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
        } else {
            Text(
                text = "✦ $label",
                style = MaterialTheme.typography.titleLarge,
                color = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    val cardSurface = MaterialTheme.colorScheme.surface
    val cardOutline = MaterialTheme.colorScheme.outline
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val cr = CornerRadius(16.dp.toPx())
                drawRoundRect(color = cardSurface, cornerRadius = cr)
                drawRoundRect(
                    color = cardOutline.copy(alpha = 0.4f),
                    cornerRadius = cr,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
    ) { content() }
}

@Composable
private fun UpdateIndicator(info: UpdateInfo, label: String) {
    val uriHandler = LocalUriHandler.current
    val infiniteTransition = rememberInfiniteTransition(label = "ripple")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleProgress"
    )
    val dotColor = Color(0xFFFFA726)

    Row(
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) { uriHandler.openUri(info.releaseUrl) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Canvas(modifier = Modifier.size(14.dp)) {
            val dotRadius = 3.5.dp.toPx()
            val maxSpread = 8.dp.toPx()
            drawCircle(
                color = dotColor.copy(alpha = (1f - progress) * 0.75f),
                radius = dotRadius + progress * maxSpread
            )
            drawCircle(color = dotColor, radius = dotRadius)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = dotColor
        )
    }
}

@Composable
private fun UsernameField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isFocused: Boolean,
    isError: Boolean,
    interactionSource: MutableInteractionSource
) {
    Box(
        modifier = if (isFocused)
            Modifier.glow(GlowPurple, radius = 16.dp, cornerRadius = 8.dp, alpha = 0.85f, topInset = 8.dp)
        else Modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            isError = isError,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            interactionSource = interactionSource,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ProfileHeaderRow(
    inSelection: Boolean,
    langBias: Float,
    isJoined: Boolean,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    onLeave: () -> Unit,
    onBack: () -> Unit
) {
    val s = LocalAppStrings.current

    val backAlpha by animateFloatAsState(
        targetValue = if (inSelection) 0f else 1f,
        animationSpec = tween(300),
        label = "backAlpha"
    )
    // Keep the last non-selection state so the button stays rendered while fading out.
    val frozenIsJoined = remember { mutableStateOf(isJoined) }
    SideEffect { if (!inSelection) frozenIsJoined.value = isJoined }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        if (backAlpha > 0f) {
            Text(
                text = if (frozenIsJoined.value) "← ${s.leave}" else "← ${s.back}",
                style = MaterialTheme.typography.bodyMedium,
                color = NavButtonColor,
                modifier = Modifier
                    .alpha(backAlpha)
                    .layout { measurable, constraints ->
                        val hPx = 24.dp.roundToPx()
                        val vPx = 20.dp.roundToPx()
                        val placeable = measurable.measure(constraints)
                        layout(placeable.width - hPx * 2, placeable.height - vPx * 2) {
                            placeable.place(-hPx, -vPx)
                        }
                    }
                    .clickable(
                        enabled = !inSelection,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = if (frozenIsJoined.value) onLeave else onBack
                    )
                    .padding(vertical = 20.dp, horizontal = 24.dp)
            )
        }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = BiasAlignment(horizontalBias = langBias, verticalBias = 0f)
        ) {
            GradientLangToggle(currentLanguage = currentLanguage, onLanguageChange = onLanguageChange)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UsernameShowScreen(
    vm: MainViewModel = viewModel(),
    onNavigateToMaintenance: () -> Unit = {},
    currentLanguage: String = "en",
    onLanguageChange: (String) -> Unit = {},
    onShowDisclaimer: () -> Unit = {}
) {
    val s = LocalAppStrings.current
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    val startupComplete by vm.startupComplete.collectAsState()
    val currentRoomCode by vm.roomCode.collectAsState()
    val currentUsername by vm.username.collectAsState()
    val currentShowId by vm.selectedShowId.collectAsState()
    val shows by vm.shows.collectAsState()
    val members by vm.members.collectAsState()
    val updateInfo by vm.updateInfo.collectAsState()

    var noRoomState by remember { mutableStateOf(NoRoomState.SELECTION) }
    var usernameText by remember { mutableStateOf(vm.savedUsername ?: "") }
    var roomCodeValue by remember { mutableStateOf(TextFieldValue(vm.savedRoomCode ?: "")) }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var usernameFieldError by remember { mutableStateOf(false) }
    var roomCodeFieldError by remember { mutableStateOf(false) }

    var showRenameDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = currentRoomCode != null) {
        val saved = vm.savedRoomCode
        if (saved != null) roomCodeValue = TextFieldValue(saved)
        noRoomState = NoRoomState.JOINING
        vm.leaveRoom()
    }

    BackHandler(enabled = currentRoomCode == null && noRoomState != NoRoomState.SELECTION) {
        noRoomState = NoRoomState.SELECTION
        errorText = null
    }
    var renameText by remember { mutableStateOf(TextFieldValue("")) }
    var renameError by remember { mutableStateOf<String?>(null) }
    var renameLoading by remember { mutableStateOf(false) }

    var titleTapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }

    val usernameInteraction = remember { MutableInteractionSource() }
    val usernameFocused by usernameInteraction.collectIsFocusedAsState()
    val codeInteraction = remember { MutableInteractionSource() }
    val codeFocused by codeInteraction.collectIsFocusedAsState()

    val contentState = when {
        currentRoomCode != null && members.isNotEmpty() -> ContentState.JOINED
        noRoomState == NoRoomState.CREATING -> ContentState.CREATING
        noRoomState == NoRoomState.JOINING -> ContentState.JOINING
        else -> ContentState.SELECTION
    }

    LaunchedEffect(currentRoomCode) {
        if (currentRoomCode == null) {
            val saved = vm.savedRoomCode
            if (saved != null) roomCodeValue = TextFieldValue(saved)
            if (noRoomState != NoRoomState.JOINING) {
                noRoomState = NoRoomState.SELECTION
            }
            errorText = null
            usernameFieldError = false
            roomCodeFieldError = false
            isLoading = false
        }
    }

    LaunchedEffect(noRoomState) {
        if (noRoomState == NoRoomState.JOINING) {
            val saved = vm.savedRoomCode
            if (saved != null) roomCodeValue = TextFieldValue(saved)
        }
    }

    LaunchedEffect(codeFocused) {
        if (codeFocused && roomCodeValue.text.isNotEmpty()) {
            roomCodeValue = roomCodeValue.copy(selection = TextRange(0, roomCodeValue.text.length))
        }
    }

    if (!startupComplete) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text(s.renameUser, style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val renameFocusRequester = remember { FocusRequester() }
                    LaunchedEffect(Unit) { renameFocusRequester.requestFocus() }
                    OutlinedTextField(
                        value = renameText,
                        onValueChange = { tfv ->
                            val capped = tfv.text.take(2)
                            renameText = tfv.copy(text = capped, selection = TextRange(capped.length))
                            renameError = null
                            if (capped.length == 2) keyboardController?.hide()
                        },
                        label = { Text(s.username) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        ),
                        modifier = Modifier.fillMaxWidth().focusRequester(renameFocusRequester)
                    )
                    if (renameError != null) {
                        Text(renameError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            renameLoading = true
                            val result = vm.renameUser(renameText.text.trim())
                            renameLoading = false
                            if (result.isSuccess) {
                                showRenameDialog = false
                            } else {
                                val msg = result.exceptionOrNull()?.message ?: ""
                                renameError = when {
                                    msg.contains("already taken", ignoreCase = true) -> s.usernameAlreadyTaken
                                    else -> msg.ifEmpty { s.usernameAlreadyTaken }
                                }
                            }
                        }
                    },
                    enabled = renameText.text.trim().length == 2 && !renameLoading
                ) {
                    if (renameLoading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    else Text(s.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text(s.cancel) }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(top = 10.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val now = System.currentTimeMillis()
                    if (now - lastTapTime > 2000) titleTapCount = 0
                    titleTapCount++
                    lastTapTime = now
                    if (titleTapCount >= 5) {
                        titleTapCount = 0
                        onNavigateToMaintenance()
                    }
                }
        ) {
            Image(
                painter = painterResource(R.drawable.est_banner),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
            SparkleOverlay(modifier = Modifier.matchParentSize())
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val inSelection = currentRoomCode == null && noRoomState == NoRoomState.SELECTION
            val langBias by animateFloatAsState(
                targetValue = if (inSelection) 0f else 1f,
                animationSpec = tween(300),
                label = "langBias"
            )
            ProfileHeaderRow(
                inSelection = inSelection,
                langBias = langBias,
                isJoined = currentRoomCode != null,
                currentLanguage = currentLanguage,
                onLanguageChange = onLanguageChange,
                onLeave = {
                    val saved = vm.savedRoomCode
                    if (saved != null) roomCodeValue = TextFieldValue(saved)
                    noRoomState = NoRoomState.JOINING
                    vm.leaveRoom()
                },
                onBack = {
                    noRoomState = NoRoomState.SELECTION
                    errorText = null
                    usernameFieldError = false
                    roomCodeFieldError = false
                }
            )

            AnimatedContent(
                targetState = contentState,
                transitionSpec = {
                    val dir = if (targetState.level >= initialState.level) 1 else -1
                    (slideInHorizontally(tween(300)) { dir * it } + fadeIn(tween(200)) togetherWith
                    slideOutHorizontally(tween(300)) { -dir * it } + fadeOut(tween(200)))
                        .using(SizeTransform(clip = false))
                },
                modifier = Modifier.fillMaxWidth(),
                label = "profileContent"
            ) { state ->
                when (state) {

                // ── JOINED ─────────────────────────────────────────────────────────────
                ContentState.JOINED -> {
                // Freeze room code and members so they stay visible during the exit animation
                // even after the ViewModel clears them on leaveRoom().
                val lastRoomCode = remember { mutableStateOf(currentRoomCode ?: "") }
                val lastMembers = remember { mutableStateOf(members) }
                val lastUsername = remember { mutableStateOf(currentUsername ?: "") }
                val lastShowId = remember { mutableStateOf(currentShowId) }
                SideEffect {
                    if (currentRoomCode != null) {
                        lastRoomCode.value = currentRoomCode!!
                        if (currentUsername != null) lastUsername.value = currentUsername!!
                        if (currentShowId != null) lastShowId.value = currentShowId
                    }
                    if (members.isNotEmpty()) lastMembers.value = members
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                // ── Room code + Members (merged) ───────────────────────────
                SectionCard {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Room code
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                s.roomCode.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    lastRoomCode.value,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = AccentBlue,
                                    letterSpacing = 4.sp,
                                )
                                Spacer(Modifier.weight(1f))
                                Row(
                                    modifier = Modifier.clickable {
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, lastRoomCode.value)
                                        }
                                        context.startActivity(Intent.createChooser(intent, null))
                                    },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = s.shareRoomCode, tint = AccentBlue, modifier = Modifier.size(28.dp))
                                    Text(
                                        s.aftershowShare,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = AccentBlue,
                                    )
                                }
                            }
                        }

                        // Divider
                        Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)))

                        // Members
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                s.members.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                lastMembers.value.values.sorted().forEach { name ->
                                    val isMe = name == lastUsername.value
                                    Box(
                                        modifier = Modifier
                                            .clip(PillShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .then(if (isMe) Modifier.clickable {
                                                val name0 = currentUsername ?: ""
                                                renameText = TextFieldValue(text = name0, selection = TextRange(name0.length))
                                                renameError = null
                                                showRenameDialog = true
                                            } else Modifier)
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isMe) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Text(
                                                    name,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        } else {
                                            Text(
                                                name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Select show ────────────────────────────────────────────
                SectionCard {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            s.show.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        ShowSegmentedControl(
                            shows = shows,
                            pickedShowId = currentShowId ?: lastShowId.value,
                            showLabel = s::showLabel,
                            onPick = { vm.selectShow(it) }
                        )
                    }
                }
                } // end JOINED Column
                }

                // ── NO_ROOM ────────────────────────────────────────────────────────────
                ContentState.SELECTION -> {
                    Column(
                        modifier = Modifier.padding(top = 64.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                    GradientSaveButton(
                        enabled = true,
                        isLoading = false,
                        label = s.createRoom,
                        onClick = { noRoomState = NoRoomState.CREATING; errorText = null }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)))
                        Text(
                            text = s.or,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                drawRoundRect(
                                    brush = GradientPink,
                                    cornerRadius = CornerRadius(100.dp.toPx()),
                                    style = Stroke(width = 1.5.dp.toPx())
                                )
                            }
                            .clip(PillShape)
                            .clickable { noRoomState = NoRoomState.JOINING; errorText = null }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✦ ${s.joinRoom}",
                            style = MaterialTheme.typography.titleLarge.copy(brush = GradientPink)
                        )
                    }
                    } // end extra-spacing Column
                }

                // ── CREATING ───────────────────────────────────────────────────────────
                ContentState.CREATING -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                    SectionCard {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(s.username.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            UsernameField(
                                value = usernameText,
                                onValueChange = { text ->
                                    val capped = text.take(2)
                                    usernameText = capped
                                    errorText = null
                                    usernameFieldError = false
                                    roomCodeFieldError = false
                                    if (capped.length == 2) keyboardController?.hide()
                                },
                                label = s.createNewUsername,
                                isFocused = usernameFocused,
                                isError = usernameFieldError,
                                interactionSource = usernameInteraction
                            )
                            if (errorText != null) {
                                Text(errorText!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    GradientSaveButton(
                        enabled = usernameText.trim().length == 2 && !isLoading,
                        isLoading = isLoading,
                        label = s.createRoom,
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorText = null
                                val result = vm.createRoom(usernameText.trim())
                                if (result.isFailure) {
                                    isLoading = false
                                    val msg = result.exceptionOrNull()?.message ?: ""
                                    usernameFieldError = true
                                    errorText = when {
                                        msg.contains("already taken", ignoreCase = true) -> s.usernameAlreadyTaken
                                        else -> msg.ifEmpty { null }
                                    }
                                }
                                // On success: isLoading stays true; the button keeps spinning
                                // until members arrive and the active state renders.
                            }
                        }
                    )
                    }
                }

                // ── JOINING ────────────────────────────────────────────────────────────
                ContentState.JOINING -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                    SectionCard {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(s.username.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            UsernameField(
                                value = usernameText,
                                onValueChange = { text ->
                                    val capped = text.take(2)
                                    usernameText = capped
                                    errorText = null
                                    usernameFieldError = false
                                    roomCodeFieldError = false
                                    if (capped.length == 2) keyboardController?.hide()
                                },
                                label = s.createNewUsername,
                                isFocused = usernameFocused,
                                isError = usernameFieldError,
                                interactionSource = usernameInteraction
                            )

                            Text(s.roomCode.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Box(
                                modifier = if (codeFocused)
                                    Modifier.glow(GlowPurple, radius = 16.dp, cornerRadius = 8.dp, alpha = 0.85f, topInset = 8.dp)
                                else Modifier
                            ) {
                                OutlinedTextField(
                                    value = roomCodeValue,
                                    onValueChange = { value ->
                                        val capped = value.text.uppercase().take(6)
                                        if (capped.length == 6 && capped.length > roomCodeValue.text.length) keyboardController?.hide()
                                        roomCodeValue = value.copy(text = capped)
                                        errorText = null
                                        usernameFieldError = false
                                        roomCodeFieldError = false
                                    },
                                    label = { Text(s.enterRoomCode) },
                                    singleLine = true,
                                    isError = roomCodeFieldError,
                                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                    interactionSource = codeInteraction,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (errorText != null) {
                                Text(errorText!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    GradientSaveButton(
                        enabled = usernameText.trim().length == 2 && roomCodeValue.text.length == 6 && !isLoading,
                        isLoading = isLoading,
                        label = s.joinRoom,
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorText = null
                                val result = vm.joinRoom(roomCodeValue.text.trim(), usernameText.trim())
                                if (result.isFailure) {
                                    isLoading = false
                                    val msg = result.exceptionOrNull()?.message ?: ""
                                    when {
                                        msg.contains("not found", ignoreCase = true) -> {
                                            roomCodeFieldError = true
                                            errorText = s.roomNotFound
                                        }
                                        msg.contains("already taken", ignoreCase = true) -> {
                                            usernameFieldError = true
                                            errorText = s.usernameAlreadyTaken
                                        }
                                        else -> {
                                            usernameFieldError = true
                                            roomCodeFieldError = true
                                            errorText = msg
                                        }
                                    }
                                }
                                // On success: isLoading stays true; the button keeps spinning
                                // until members arrive and the active state renders.
                            }
                        }
                    )
                    }
                }
                } // end AnimatedContent when
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    if (updateInfo is UpdateCheckResult.Available) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp, start = 24.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = s.disclaimerLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onShowDisclaimer
                )
            )
            Text(
                "·",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
            )
            UpdateIndicator(info = (updateInfo as UpdateCheckResult.Available).info, label = s.updateAvailable)
        }
    } else {
        Text(
            text = s.disclaimerLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .layout { measurable, constraints ->
                    val hPx = 24.dp.roundToPx()
                    val topPx = 20.dp.roundToPx()
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width - hPx * 2, placeable.height - topPx) {
                        placeable.place(-hPx, -topPx)
                    }
                }
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onShowDisclaimer)
                .padding(top = 20.dp, bottom = 12.dp, start = 24.dp, end = 24.dp),
            textAlign = TextAlign.Center
        )
    }
    } // end Box
}
