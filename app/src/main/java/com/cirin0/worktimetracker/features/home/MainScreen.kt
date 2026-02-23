package com.cirin0.worktimetracker.features.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cirin0.worktimetracker.core.ui.QRCodeScannerScreen
import com.cirin0.worktimetracker.core.utils.DateUtils
import com.cirin0.worktimetracker.features.timeentries.data.model.TimeEntry
import com.cirin0.worktimetracker.features.timeentries.presentation.TimeEntriesState
import com.cirin0.worktimetracker.features.timeentries.presentation.TimeEntriesViewModel
import java.util.Locale

@Composable
fun MainScreen(
    onNavigateToDetail: (Int) -> Unit = {},
    viewModel: TimeEntriesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Відстеження часу",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = {
                viewModel.loadActiveEntry()
                viewModel.loadTimeEntries()
            }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Оновити"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (state.showServerUnavailableWarning) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Попередження",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = "Сервер недоступний - показано збережені дані",
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.activeEntry != null -> {
                ActiveEntryCard(state.activeEntry!!, state, viewModel)
            }

            else -> {
                StartEntryCard(state, viewModel)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Історія записів",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            state.isLoadingList -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.listError != null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Помилка: ${state.listError}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(onClick = { viewModel.loadTimeEntries() }) {
                        Text("Спробувати знову")
                    }
                }
            }

            state.timeEntries.isEmpty() -> {
                Text(
                    text = "Немає записів",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.timeEntries.forEach { entry ->
                        TimeEntryListItem(
                            entry = entry,
                            onClick = { onNavigateToDetail(entry.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StartEntryCard(
    state: TimeEntriesState,
    viewModel: TimeEntriesViewModel
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
        if (isGranted) {
            viewModel.startTimeEntry()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onCameraPermissionResult(isGranted)
        if (isGranted) {
            viewModel.showQRScanner()
        }
    }

    LaunchedEffect(state.qrCodeScanSuccess) {
        if (state.qrCodeScanSuccess) {
            if (!viewModel.hasLocationPermission()) {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                viewModel.startTimeEntry()
            }
        }
    }

    val user = state.user
    val isHybrid = user?.isHybrid() == true
    val isOffice = user?.requiresGPS() == true
    val isRemote = user?.isRemote() == true

    val needsGPS = when {
        isRemote -> false
        isOffice -> true
        isHybrid -> state.isInOffice
        else -> true
    }

    val needsQRScan = isOffice

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Почати роботу",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            user?.let {
                Text(
                    text = when {
                        isRemote -> "Режим: Віддалена робота"
                        isOffice -> "Режим: Офіс (потрібна геолокація та QR-код)"
                        isHybrid -> "Режим: Гібридний"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isHybrid) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!state.isLoading && !state.isLoadingLocation) {
                                    viewModel.toggleIsInOffice(!state.isInOffice)
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Я працюю з офісу",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            if (state.isInOffice) {
                                Text(
                                    text = "Для роботи з офісу потрібна геолокація",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = state.isInOffice,
                            onCheckedChange = { viewModel.toggleIsInOffice(it) },
                            enabled = !state.isLoading && !state.isLoadingLocation
                        )
                    }
                }
            }

            if (state.error != null) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (state.locationPermissionDenied && needsGPS) {
                Text(
                    text = "Для відстеження часу потрібен доступ до локації. Будь ласка, надайте дозвіл у налаштуваннях.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (state.cameraPermissionDenied) {
                Text(
                    text = "Для сканування QR-коду потрібен доступ до камери. Будь ласка, надайте дозвіл у налаштуваннях.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = state.startComment,
                onValueChange = viewModel::updateStartComment,
                label = { Text("Коментар до початку (необов'язково)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading && !state.isLoadingLocation
            )

            Button(
                onClick = {
                    when {
                        needsQRScan -> {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }

                        needsGPS && !viewModel.hasLocationPermission() -> {
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }

                        else -> {
                            viewModel.startTimeEntry()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading && !state.isLoadingLocation
            ) {
                if (state.isLoadingLocation) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Отримання локації...")
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Почати відстеження")
                }
            }

            if (state.showQRScanner) {
                Dialog(
                    onDismissRequest = { viewModel.hideQRScanner() },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    QRCodeScannerScreen(
                        onQRCodeScanned = { qrCode ->
                            viewModel.onQRCodeScanned(qrCode)
                        },
                        onDismiss = { viewModel.hideQRScanner() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveEntryCard(
    entry: TimeEntry,
    state: TimeEntriesState,
    viewModel: TimeEntriesViewModel
) {
    var isPinVisible by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Активне відстеження часу",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Початок: ${formatTime(entry.startTime)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            entry.startComment?.let {
                Text(
                    text = "Коментар: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            HorizontalDivider()

            if (state.error != null) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            OutlinedTextField(
                value = state.stopComment,
                onValueChange = viewModel::updateStopComment,
                label = { Text("Коментар до зупинки (необов'язково)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            )

            OutlinedTextField(
                value = state.pinCode,
                onValueChange = viewModel::updatePinCode,
                label = { Text("PIN-код (4 цифри)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
                visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                trailingIcon = {
                    IconButton(onClick = { isPinVisible = !isPinVisible }) {
                        Icon(
                            imageVector = if (isPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (isPinVisible) "Сховати PIN" else "Показати PIN"
                        )
                    }
                },
                isError = state.pinCode.isNotEmpty() && state.pinCode.length != 4,
                supportingText = if (state.pinCode.isNotEmpty() && state.pinCode.length != 4) {
                    { Text("PIN-код має містити 4 цифри") }
                } else null
            )

            Button(
                onClick = { viewModel.stopTimeEntry() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading && state.pinCode.length == 4,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Зупинити відстеження")
                }
            }
        }
    }
}

private fun formatTime(timeString: String): String {
    return DateUtils.formatTime(timeString)
}

@Composable
private fun TimeEntryListItem(
    entry: TimeEntry,
    onClick: () -> Unit
) {
    val startTimeFormatted = remember(entry.startTime) { formatTime(entry.startTime) }
    val stopTimeFormatted = remember(entry.stopTime) {
        entry.stopTime?.let { formatTime(it) }
    }
    val durationFormatted = remember(entry.duration) {
        formatDuration(entry.duration ?: 0)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.stopTime == null) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${entry.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (entry.stopTime == null) {
                    Text(
                        text = "⏱️ Активний",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = durationFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Початок:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = startTimeFormatted,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            stopTimeFormatted?.let { stopTime ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Зупинка:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stopTime,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (!entry.startComment.isNullOrBlank()) {
                Text(
                    text = "💬 ${entry.startComment}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%dг %dхв", hours, minutes)
    } else {
        String.format(Locale.getDefault(), "%dхв", minutes)
    }
}
