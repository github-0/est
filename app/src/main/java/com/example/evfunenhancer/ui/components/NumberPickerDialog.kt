package com.example.evfunenhancer.ui.components

import android.view.Gravity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import com.example.evfunenhancer.ui.strings.LocalAppStrings

@Composable
fun NumberPickerDialog(
    flag: String,
    countryName: String,
    currentValue: Int,
    showWinnerGuess: Boolean,
    currentUserGuessRank: Int?,
    onGuessChanged: (Int?) -> Unit,
    onConfirm: (points: Int, chipScreenCenter: Offset) -> Unit,
    onDismiss: () -> Unit
) {
    val s = LocalAppStrings.current
    val chipPositions = remember { mutableStateMapOf<Int, Offset>() }
    var showPodiums by remember { mutableStateOf(false) }
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
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$flag ", fontSize = 33.sp)
                    Text(countryName, style = MaterialTheme.typography.titleLarge)
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1..4, 5..8, 9..12).forEach { range ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            range.forEach { value ->
                                PointChip(
                                    value = value,
                                    isCurrent = value == currentValue,
                                    color = heatColor(value),
                                    onClick = { onConfirm(value, chipPositions[value] ?: Offset.Zero) },
                                    onScreenCenterPositioned = { offset -> chipPositions[value] = offset }
                                )
                            }
                        }
                    }
                }

                if (showWinnerGuess) Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (showPodiums) "↑ ${s.winnerGuess} ↑" else "↓ ${s.winnerGuess} ↓",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .clickable { showPodiums = !showPodiums }
                            .padding(vertical = 5.dp, horizontal = 12.dp)
                    )
                    if (showPodiums) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(
                                    Triple(1, Color(0xFFFFD700), "🥇"),
                                    Triple(2, Color(0xFFB0B8C0), "🥈"),
                                    Triple(3, Color(0xFFCD7F32), "🥉"),
                                ).forEach { (rank, color, emoji) ->
                                    val selected = currentUserGuessRank == rank
                                    Surface(
                                        onClick = { onGuessChanged(if (selected) null else rank) },
                                        shape = CircleShape,
                                        color = if (selected) color else Color.Transparent,
                                        border = BorderStroke(
                                            if (selected) 2.5.dp else 2.dp,
                                            if (selected) color else color.copy(alpha = 0.55f)
                                        ),
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = emoji,
                                                fontSize = 20.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
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
    onScreenCenterPositioned: (Offset) -> Unit
) {
    val view = LocalView.current
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = color,
        contentColor = Color.White,
        border = if (isCurrent) BorderStroke(2.5.dp, Color.White) else null,
        modifier = Modifier
            .size(52.dp)
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
