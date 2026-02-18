package com.cirin0.worktimetracker.features.timeentries.presentation

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cirin0.worktimetracker.features.timeentries.data.model.TimeEntry
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TimeEntriesScreen(
    onNavigateToDetail: (Int) -> Unit = {},
    viewModel: TimeEntriesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        item {
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
                Row {
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
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
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
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
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
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (state.isLoadingList) {
            item {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (state.listError != null) {
            item {
                Text(
                    text = "Помилка завантаження: ${state.listError}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else if (state.timeEntries.isEmpty()) {
            item {
                Text(
                    text = "Немає записів",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(
                items = state.timeEntries,
                key = { entry -> entry.id },
                contentType = { "timeEntry" }
            ) { entry ->
                TimeEntryListItem(
                    entry = entry,
                    onClick = { onNavigateToDetail(entry.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
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

    val user = state.user
    val isHybrid = user?.isHybrid() == true
    val isOffice = user?.requiresGPS() == true
    val isRemote = user?.isRemote() == true

    // Determine if GPS is needed
    val needsGPS = when {
        isRemote -> false
        isOffice -> true
        isHybrid -> state.isInOffice
        else -> true
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Почати роботу",
                style = MaterialTheme.typography.headlineSmall
            )

            // Show work mode info
            user?.let {
                Text(
                    text = when {
                        isRemote -> "Режим: Віддалена робота"
                        isOffice -> "Режим: Офіс (потрібна геолокація)"
                        isHybrid -> "Режим: Гібридний"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Switch for hybrid mode
            if (isHybrid) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!state.isLoading && !state.isLoadingLocation) {
                                viewModel.toggleIsInOffice(!state.isInOffice)
                            }
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Я працюю з офісу",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = state.isInOffice,
                        onCheckedChange = { viewModel.toggleIsInOffice(it) },
                        enabled = !state.isLoading && !state.isLoadingLocation
                    )
                }
                if (state.isInOffice) {
                    Text(
                        text = "Для роботи з офісу потрібна геолокація",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

            OutlinedTextField(
                value = state.startComment,
                onValueChange = viewModel::updateStartComment,
                label = { Text("Коментар до початку (необов'язково)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading && !state.isLoadingLocation
            )

            Button(
                onClick = {
                    if (needsGPS && !viewModel.hasLocationPermission()) {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    } else {
                        viewModel.startTimeEntry()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading && !state.isLoadingLocation
            ) {
                if (state.isLoadingLocation) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(16.dp)
                            .height(16.dp),
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
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Початок: ${formatTime(entry.startTime)}",
                style = MaterialTheme.typography.bodyLarge
            )

            entry.startComment?.let {
                Text(
                    text = "Коментар: $it",
                    style = MaterialTheme.typography.bodyMedium
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
                onClick = {
                    viewModel.stopTimeEntry()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading && state.pinCode.length == 4,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Зупинити відстеження")
            }
        }
    }
}

private fun formatTime(timeString: String): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateTime = LocalDateTime.parse(timeString, formatter)
        dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (_: Exception) {
        timeString
    }
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

