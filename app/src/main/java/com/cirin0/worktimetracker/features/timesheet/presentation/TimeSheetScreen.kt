package com.cirin0.worktimetracker.features.timesheet.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cirin0.worktimetracker.R
import com.cirin0.worktimetracker.core.localization.AppLocaleManager
import com.cirin0.worktimetracker.core.utils.DateUtils
import com.cirin0.worktimetracker.features.timeentries.data.model.TimeEntry
import com.cirin0.worktimetracker.features.timesheet.data.model.PeriodSummary
import com.cirin0.worktimetracker.features.timesheet.data.model.TimeSummary
import java.time.DayOfWeek

@Composable
fun TimesheetScreen(
    viewModel: TimeSheetViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

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
                onRefresh = { viewModel.loadTimeSummary() }
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
                        OutlinedButton(onClick = { viewModel.loadTimeSummary() }) {
                            Text(stringResource(R.string.general_retry))
                        }
                    }
                }

                state.summary == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.timesheet_no_data),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    TimeSummaryContent(state.summary!!, viewModel)
                }
            }
        }
    }
}

@Composable
private fun TopBarSection(onRefresh: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(R.string.timesheet_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.timesheet_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TopBarIconButton(
            icon = Icons.Default.Refresh,
            contentDescription = stringResource(R.string.general_refresh),
            onClick = onRefresh
        )
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
private fun TimeSummaryContent(summary: TimeSummary, viewModel: TimeSheetViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatsSection(summary)

        WeeklyChartSection(summary, state.weeklyEntries)

        PeriodSection(summary)

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun StatsSection(summary: TimeSummary) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.timesheet_general_stats).uppercase(),
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
                StatRowWithIcon(
                    icon = Icons.Default.Timer,
                    label = stringResource(R.string.timesheet_total_time),
                    value = DateUtils.formatHours(summary.totalHours + summary.totalMinutes / 60.0)
                )
                RowDivider()
                StatRowWithIcon(
                    icon = Icons.AutoMirrored.Filled.List,
                    label = stringResource(R.string.timesheet_working_days),
                    value = "${summary.workingDays}"
                )
                RowDivider()
                StatRowWithIcon(
                    icon = Icons.Default.Schedule,
                    label = stringResource(R.string.timesheet_average_work_time),
                    value = DateUtils.formatHours(summary.averageWorkTime / 60.0)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.timesheet_attendance).uppercase(),
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
                AttendanceRow(
                    label = stringResource(R.string.timesheet_late_count),
                    value = "${summary.attendance.lateCount}",
                    valueColor = MaterialTheme.colorScheme.error
                )
                RowDivider()
                AttendanceRow(
                    label = stringResource(R.string.timesheet_on_time),
                    value = "${summary.attendance.onTimeCount}",
                    valueColor = MaterialTheme.colorScheme.primary
                )
                RowDivider()
                AttendanceRow(
                    label = stringResource(R.string.timesheet_early_leave),
                    value = "${summary.attendance.earlyLeaveCount}",
                    valueColor = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        if (summary.attendance.lateCount > 0 || summary.attendance.overtimeCount > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (summary.attendance.lateCount > 0) {
                        Text(
                            text = stringResource(
                                R.string.timesheet_average_late_format,
                                String.format(
                                    AppLocaleManager.getCurrentLocale(),
                                    "%.1f",
                                    summary.attendance.averageLateMinutes
                                )
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    if (summary.attendance.overtimeCount > 0) {
                        Text(
                            text = stringResource(
                                R.string.timesheet_overtime_format,
                                summary.attendance.overtimeCount,
                                summary.attendance.totalOvertimeMinutes
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceRow(
    label: String,
    value: String,
    valueColor: Color
) {
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
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun PeriodSection(summary: TimeSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.timesheet_periods).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )

        PeriodCard(
            title = stringResource(R.string.timesheet_today),
            period = summary.summary.today,
            icon = Icons.Default.CalendarToday,
            iconColor = MaterialTheme.colorScheme.secondary,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )

        PeriodCard(
            title = stringResource(R.string.timesheet_this_week),
            period = summary.summary.week,
            icon = Icons.Default.DateRange,
            iconColor = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )

        PeriodCard(
            title = stringResource(R.string.timesheet_this_month),
            period = summary.summary.month,
            icon = Icons.Default.CalendarMonth,
            iconColor = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PeriodCard(
    title: String,
    period: PeriodSummary,
    icon: ImageVector,
    iconColor: Color,
    containerColor: Color,
    contentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(34.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = iconColor.copy(alpha = 0.2f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = contentColor
                    )
                    Text(
                        text = stringResource(
                            R.string.timesheet_period_summary,
                            period.workingDays,
                            period.lateCount,
                            period.earlyCount
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }

            Surface(shape = RoundedCornerShape(10.dp), color = iconColor.copy(alpha = 0.15f)) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = DateUtils.formatHours(period.hours + period.minutes / 60.0),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
            }
        }
    }
}

@Composable
private fun StatRowWithIcon(
    icon: ImageVector,
    label: String,
    value: String
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
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
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

@Composable
private fun WeeklyChartSection(summary: TimeSummary, weeklyEntries: List<TimeEntry>) {
    val today = java.time.LocalDate.now()
    val startOfWeek = today.with(DayOfWeek.MONDAY)

    val entriesByDay = weeklyEntries
        .filter { entry ->
            try {
                val entryDate = java.time.LocalDate.parse(entry.date)
                !entryDate.isBefore(startOfWeek) && !entryDate.isAfter(today)
            } catch (e: Exception) {
                false
            }
        }
        .groupBy { entry ->
            try {
                java.time.LocalDate.parse(entry.date).dayOfWeek
            } catch (e: Exception) {
                null
            }
        }

    val weekData = listOf(
        DayOfWeek.MONDAY to stringResource(R.string.timesheet_monday_short),
        DayOfWeek.TUESDAY to stringResource(R.string.timesheet_tuesday_short),
        DayOfWeek.WEDNESDAY to stringResource(R.string.timesheet_wednesday_short),
        DayOfWeek.THURSDAY to stringResource(R.string.timesheet_thursday_short),
        DayOfWeek.FRIDAY to stringResource(R.string.timesheet_friday_short),
        DayOfWeek.SATURDAY to stringResource(R.string.timesheet_saturday_short),
        DayOfWeek.SUNDAY to stringResource(R.string.timesheet_sunday_short)
    ).map { (dayOfWeek, label) ->
        val dayEntries = entriesByDay[dayOfWeek] ?: emptyList()
        val totalSeconds = dayEntries.sumOf { it.duration ?: 0 }
        val hours = totalSeconds / 3600.0
        DayData(label, hours)
    }

    val weekHours = summary.summary.week.hours + summary.summary.week.minutes / 60.0
    val maxHours = weekData.maxOfOrNull { it.hours }?.coerceAtLeast(8.0) ?: 12.0

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.timesheet_weekly_chart).uppercase(),
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
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.timesheet_this_week),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = DateUtils.formatHours(weekHours),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.timesheet_chart_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                WeeklyBarChart(
                    data = weekData,
                    maxHours = maxHours,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
        }
    }
}

data class DayData(val label: String, val hours: Double)


@Composable
private fun WeeklyBarChart(
    data: List<DayData>,
    maxHours: Double,
    modifier: Modifier = Modifier
) {
    val barColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .width(36.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 4 downTo 0) {
                    Text(
                        text = "${(maxHours * i / 4).toInt()}г",
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    val totalBars = data.size
                    val totalSpacing = canvasWidth * 0.3f
                    val totalBarWidth = canvasWidth - totalSpacing
                    val barWidth = totalBarWidth / totalBars
                    val spacing = totalSpacing / (totalBars + 1)

                    for (i in 0..4) {
                        val y = canvasHeight * (i / 4f)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    data.forEachIndexed { index, dayData ->
                        val barHeight = if (maxHours > 0) {
                            (dayData.hours / maxHours * canvasHeight).toFloat()
                        } else 0f

                        val x = spacing + index * (barWidth + spacing)

                        if (barHeight > 0) {
                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(x, canvasHeight - barHeight),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.width(36.dp))
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                data.forEach { dayData ->
                    Text(
                        text = dayData.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
