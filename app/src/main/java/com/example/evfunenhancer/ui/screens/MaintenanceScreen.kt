package com.example.evfunenhancer.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evfunenhancer.ui.strings.LocalAppStrings
import com.example.evfunenhancer.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(vm: MainViewModel = viewModel(), onBack: () -> Unit = {}) {
    val s = LocalAppStrings.current

    var refreshKey by remember { mutableIntStateOf(0) }
    var lastCheckedTime by remember { mutableStateOf<LocalTime?>(null) }
    var firestoreOnline by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(refreshKey) {
        firestoreOnline = null
        lastCheckedTime = null
        vm.observeFirestoreConnectivity().collectLatest { online ->
            firestoreOnline = online
            if (lastCheckedTime == null) lastCheckedTime = LocalTime.now()
        }
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
        ) {
            Text(
                s.maintenanceFirebaseUid,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            Text(
                vm.myUid ?: "—",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(s.maintenanceFirestoreStatus, style = MaterialTheme.typography.titleSmall)
                IconButton(onClick = { refreshKey++ }) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = s.maintenanceRefreshContentDescription
                    )
                }
            }

            val dotColor = when (firestoreOnline) {
                true -> Color(0xFF4CAF50)
                false -> MaterialTheme.colorScheme.error
                null -> MaterialTheme.colorScheme.outline
            }
            val statusText = when (firestoreOnline) {
                true -> s.maintenanceStatusOnline
                false -> s.maintenanceStatusOffline
                null -> s.maintenanceStatusChecking
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(dotColor, CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(statusText, style = MaterialTheme.typography.bodyLarge)
            }

            val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
            if (lastCheckedTime != null) {
                Text(
                    s.maintenanceLastChecked(
                        lastCheckedTime!!.format(timeFormatter)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 2.dp, start = 18.dp)
                )
            }
        }
    }
}
