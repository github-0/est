package com.example.evfunenhancer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evfunenhancer.BuildConfig
import com.example.evfunenhancer.data.UpdateCheckResult
import com.example.evfunenhancer.ui.strings.LocalAppStrings
import com.example.evfunenhancer.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(vm: MainViewModel = viewModel(), onBack: () -> Unit = {}) {
    val s = LocalAppStrings.current

    val updateInfo by vm.updateInfo.collectAsState()
    var refreshKey by remember { mutableIntStateOf(0) }
    var firestoreOnline by remember { mutableStateOf<Boolean?>(null) }

    val roomCode by vm.roomCode.collectAsState()
    val isRoomCreator by vm.isRoomCreator.collectAsState()
    val creatorUid by vm.creatorUid.collectAsState()
    val members by vm.members.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showMemberListDialog by remember { mutableStateOf(false) }
    var pendingRemoval by remember { mutableStateOf<Pair<String, String>?>(null) }
    var confirmText by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var resultIsSuccess by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        vm.refreshUpdateCheck()
    }

    LaunchedEffect(refreshKey) {
        firestoreOnline = null
        vm.observeFirestoreConnectivity().collectLatest { online ->
            firestoreOnline = online
        }
    }

    // Member list dialog — shows either the member list (creator) or an explanation (non-creator)
    if (showMemberListDialog) {
        val otherMembers = members.entries
            .filter { (uid, _) -> uid != vm.myUid }
            .sortedBy { (_, username) -> username }

        if (isRoomCreator) {
            AlertDialog(
                onDismissRequest = { showMemberListDialog = false },
                title = { Text(s.removeMembersSelectTitle) },
                text = {
                    Column {
                        if (otherMembers.isEmpty()) {
                            Text(
                                "—",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        } else {
                            otherMembers.forEach { (uid, username) ->
                                Text(
                                    username,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showMemberListDialog = false
                                            confirmText = ""
                                            pendingRemoval = uid to username
                                        }
                                        .padding(vertical = 12.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showMemberListDialog = false }) {
                        Text(s.cancel)
                    }
                }
            )
        } else {
            val creatorUsername = creatorUid?.let { members[it] }
            AlertDialog(
                onDismissRequest = { showMemberListDialog = false },
                text = {
                    Text(
                        if (creatorUsername != null) s.removeMembersNotCreator(creatorUsername)
                        else s.removeMembersNoCreatorInfo,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showMemberListDialog = false }) {
                        Text(s.cancel)
                    }
                }
            )
        }
    }

    // Confirmation dialog — stays open while the removal is in progress so the user gets
    // immediate feedback; the result message appears right as the dialog closes.
    pendingRemoval?.let { (uidToRemove, username) ->
        var isRemoving by remember { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        AlertDialog(
            onDismissRequest = {
                if (!isRemoving) {
                    pendingRemoval = null
                    confirmText = ""
                }
            },
            title = { Text(username) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(s.removeMembersConfirmBody(username))
                    OutlinedTextField(
                        value = confirmText,
                        onValueChange = { confirmText = it },
                        singleLine = true,
                        enabled = !isRemoving,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                    if (isRemoving) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !isRemoving &&
                        confirmText.trim().equals(s.removeMembersConfirmWord, ignoreCase = true),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    onClick = {
                        isRemoving = true
                        coroutineScope.launch {
                            val result = vm.removeMember(uidToRemove)
                            pendingRemoval = null
                            confirmText = ""
                            resultIsSuccess = result.isSuccess
                            resultMessage = if (result.isSuccess) s.removeMembersSuccess
                                           else s.removeMembersFailed
                        }
                    }
                ) { Text(s.remove) }
            },
            dismissButton = {
                TextButton(
                    enabled = !isRemoving,
                    onClick = {
                        pendingRemoval = null
                        confirmText = ""
                    }
                ) { Text(s.cancel) }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text(s.maintenanceMode) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
                    }
                },
                windowInsets = WindowInsets(0)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val updateDotColor = when (updateInfo) {
                is UpdateCheckResult.Available -> Color(0xFFFFA726)
                is UpdateCheckResult.UpToDate -> Color(0xFF4CAF50)
                is UpdateCheckResult.Failed -> MaterialTheme.colorScheme.error
                UpdateCheckResult.Pending -> MaterialTheme.colorScheme.outline
            }
            val updateStatusText = when (val u = updateInfo) {
                is UpdateCheckResult.Available -> "${s.updateAvailable}: v${u.info.latestVersion}"
                is UpdateCheckResult.UpToDate -> s.updateUpToDate
                is UpdateCheckResult.Failed -> s.updateCheckFailed
                UpdateCheckResult.Pending -> s.updateChecking
            }
            val firestoreDotColor = when (firestoreOnline) {
                true -> Color(0xFF4CAF50)
                false -> MaterialTheme.colorScheme.error
                null -> MaterialTheme.colorScheme.outline
            }
            val firestoreStatusText = when (firestoreOnline) {
                true -> s.maintenanceStatusOnline
                false -> s.maintenanceStatusOffline
                null -> s.maintenanceStatusChecking
            }

            // ── Section A: Application status ──────────────────────────────
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(s.maintenanceSectionStatus, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = {
                    refreshKey++
                    vm.refreshUpdateCheck()
                }) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = s.maintenanceRefreshContentDescription
                    )
                }
            }

            // 2×2 status table
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        s.maintenanceAppVersion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .background(updateDotColor, CircleShape)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            updateStatusText,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        s.maintenanceFirestoreStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .background(firestoreDotColor, CircleShape)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(firestoreStatusText, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Firebase UID
            Text(
                s.maintenanceFirebaseUid,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 12.dp, bottom = 2.dp)
            )
            Text(
                vm.myUid ?: "—",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ── Section B: Maintenance tools ───────────────────────────────
            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            Text(
                s.maintenanceSectionTools,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Button(
                onClick = {
                    resultMessage = null
                    showMemberListDialog = true
                },
                enabled = roomCode != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(s.removeMembers)
            }

            resultMessage?.let { msg ->
                Text(
                    msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (resultIsSuccess) Color(0xFF4CAF50)
                            else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.padding(bottom = 16.dp))
        }
    }
}
