package com.cirin0.worktimetracker.features.timeentries.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cirin0.worktimetracker.R
import com.cirin0.worktimetracker.core.utils.DateUtils
import com.cirin0.worktimetracker.features.timeentries.data.model.TimeEntry
import java.util.Locale

@Composable
fun TimeEntryDetailScreen(
    timeEntryId: Int,
    onNavigateBack: () -> Unit,
    viewModel: TimeEntryDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(timeEntryId) {
        viewModel.loadTimeEntry(timeEntryId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopBarSection(
                onBack = onNavigateBack
            )

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(
                                R.string.general_error_with_message,
                                state.error ?: ""
                            ),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(onClick = { viewModel.loadTimeEntry(timeEntryId) }) {
                            Text(stringResource(R.string.general_retry))
                        }
                    }
                }

                state.timeEntry != null -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    TimeEntryDetailContent(state.timeEntry!!)
                }
            }
        }
    }
}

@Composable
private fun TopBarSection(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TopBarIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.general_back),
                onClick = onBack
            )
            Column {
                Text(
                    text = stringResource(R.string.time_entry_detail_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun TopBarIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TimeEntryDetailContent(timeEntry: TimeEntry) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeaderCard(timeEntry)

        TimeWorkSection(timeEntry)

        if (timeEntry.scheduledStartTime != null || timeEntry.scheduledEndTime != null ||
            (timeEntry.latenessMinutes ?: 0) > 0 || (timeEntry.earlyLeaveMinutes ?: 0) > 0 ||
            (timeEntry.overtimeMinutes ?: 0) > 0
        ) {
            ScheduleSection(timeEntry)
        }

        if (!timeEntry.startComment.isNullOrBlank() || !timeEntry.stopComment.isNullOrBlank()) {
            CommentsSection(timeEntry)
        }

        if (timeEntry.locationData != null) {
            LocationSection(timeEntry)
        }

        AdditionalSection(timeEntry)

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun HeaderCard(timeEntry: TimeEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (timeEntry.stopTime == null) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.time_entry_detail_entry_number, timeEntry.id),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (timeEntry.stopTime == null) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
                Text(
                    text = DateUtils.formatDate(timeEntry.date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (timeEntry.stopTime == null) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    }
                )
            }

            if (timeEntry.stopTime == null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = stringResource(R.string.home_active),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeWorkSection(timeEntry: TimeEntry) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.time_entry_detail_work_time).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                DetailRow(
                    icon = Icons.Default.AccessTime,
                    label = stringResource(R.string.time_entry_detail_start),
                    value = formatDateTime(timeEntry.startTime)
                )
                if (timeEntry.stopTime != null) {
                    RowDivider()
                    DetailRow(
                        icon = Icons.Default.Schedule,
                        label = stringResource(R.string.time_entry_detail_end),
                        value = formatDateTime(timeEntry.stopTime)
                    )
                    RowDivider()
                    DetailRow(
                        icon = Icons.Default.Edit,
                        label = stringResource(R.string.time_entry_detail_duration),
                        value = formatDuration(timeEntry.duration ?: 0),
                        highlighted = true
                    )
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = stringResource(R.string.time_entry_detail_not_finished),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleSection(timeEntry: TimeEntry) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.nav_schedule).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                timeEntry.scheduledStartTime?.let {
                    DetailRow(
                        icon = Icons.Default.Schedule,
                        label = stringResource(R.string.time_entry_detail_scheduled_start),
                        value = it
                    )
                }
                timeEntry.scheduledEndTime?.let {
                    if (timeEntry.scheduledStartTime != null) RowDivider()
                    DetailRow(
                        icon = Icons.Default.Schedule,
                        label = stringResource(R.string.time_entry_detail_scheduled_end),
                        value = it
                    )
                }

                timeEntry.latenessMinutes?.let {
                    if (it > 0) {
                        if (timeEntry.scheduledStartTime != null || timeEntry.scheduledEndTime != null) {
                            RowDivider()
                        }
                        WarningRow(
                            label = stringResource(R.string.timesheet_late_count),
                            value = stringResource(R.string.general_minutes_short_format, it)
                        )
                    }
                }

                timeEntry.earlyLeaveMinutes?.let {
                    if (it > 0) {
                        RowDivider()
                        WarningRow(
                            label = stringResource(R.string.timesheet_early_leave),
                            value = stringResource(R.string.general_minutes_short_format, it)
                        )
                    }
                }

                timeEntry.overtimeMinutes?.let {
                    if (it > 0) {
                        RowDivider()
                        OvertimeRow(
                            label = stringResource(R.string.time_entry_detail_overtime),
                            value = stringResource(R.string.general_minutes_short_format, it)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentsSection(timeEntry: TimeEntry) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.time_entry_detail_comments).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                timeEntry.startComment?.let {
                    CommentBox(stringResource(R.string.time_entry_detail_before_start), it)
                }
                timeEntry.stopComment?.let {
                    CommentBox(stringResource(R.string.time_entry_detail_before_finish), it)
                }
            }
        }
    }
}

@Composable
private fun LocationSection(timeEntry: TimeEntry) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.settings_permission_location).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                DetailRow(
                    icon = Icons.Default.LocationOn,
                    label = stringResource(R.string.time_entry_detail_coordinates),
                    value = "${String.format(Locale.US, "%.6f", timeEntry.locationData?.lat)}, ${
                        String.format(Locale.US, "%.6f", timeEntry.locationData?.lng)
                    }"
                )
            }
        }
    }
}

@Composable
private fun AdditionalSection(timeEntry: TimeEntry) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.time_entry_detail_additional).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                DetailRow(
                    icon = Icons.Default.Info,
                    label = stringResource(R.string.time_entry_detail_entry_type),
                    value = when (timeEntry.entryType) {
                        "manual" -> stringResource(R.string.time_entry_detail_entry_type_manual)
                        "automatic" -> stringResource(R.string.time_entry_detail_entry_type_automatic)
                        else -> timeEntry.entryType
                    }
                )
                timeEntry.createdAt?.let {
                    RowDivider()
                    DetailRow(
                        icon = Icons.Default.CalendarMonth,
                        label = stringResource(R.string.time_entry_detail_created),
                        value = formatDateTime(it)
                    )
                }
                timeEntry.updatedAt?.let {
                    if (timeEntry.createdAt != null) RowDivider()
                    DetailRow(
                        icon = Icons.Default.Edit,
                        label = stringResource(R.string.time_entry_detail_updated),
                        value = formatDateTime(it)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    highlighted: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(34.dp),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = if (highlighted) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Normal,
            color = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun WarningRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun OvertimeRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.tertiary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun CommentBox(label: String, comment: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = comment,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 62.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        thickness = 0.5.dp
    )
}

private fun formatDateTime(timeString: String): String {
    return DateUtils.formatDateTime(timeString)
}

private fun formatDuration(seconds: Int): String {
    // duration is in seconds, convert to hours
    val hours = seconds / 3600.0
    return DateUtils.formatHours(hours)
}
