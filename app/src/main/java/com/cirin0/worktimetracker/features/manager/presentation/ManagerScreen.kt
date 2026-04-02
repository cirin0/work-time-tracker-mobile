package com.cirin0.worktimetracker.features.manager.presentation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cirin0.worktimetracker.R
import com.cirin0.worktimetracker.core.ui.EmptyState
import com.cirin0.worktimetracker.core.ui.ErrorState
import com.cirin0.worktimetracker.core.ui.LoadingState
import com.cirin0.worktimetracker.core.utils.DateUtils
import com.cirin0.worktimetracker.features.manager.data.model.ActiveEmployee
import com.cirin0.worktimetracker.features.manager.data.model.PendingLeaveRequest
import kotlinx.coroutines.launch

@Composable
fun ManagerScreen(
    viewModel: ManagerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state.actionSuccess) {
        if (state.actionSuccess) {
            viewModel.clearActionSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.manager_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.manager_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 1.dp,
                onClick = {
                    viewModel.loadLeaveRequests()
                    viewModel.loadActiveEmployees()
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.general_refresh),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        PrimaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                },
                text = {
                    Text(
                        text = stringResource(R.string.manager_tab_leave_requests),
                        fontWeight = if (pagerState.currentPage == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                },
                text = {
                    Text(
                        text = stringResource(R.string.manager_tab_active_employees),
                        fontWeight = if (pagerState.currentPage == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> LeaveRequestsTab(
                    isLoading = state.isLoadingLeaveRequests,
                    error = state.leaveRequestsError,
                    requests = state.leaveRequests,
                    processingRequestId = state.processingRequestId,
                    onApprove = { viewModel.showApproveDialog(it) },
                    onReject = { viewModel.showRejectDialog(it) },
                    onRetry = { viewModel.loadLeaveRequests() }
                )

                1 -> ActiveEmployeesTab(
                    isLoading = state.isLoadingActiveEmployees,
                    error = state.activeEmployeesError,
                    employees = state.activeEmployees,
                    onRetry = { viewModel.loadActiveEmployees() }
                )
            }
        }
    }

    if (state.showCommentDialog) {
        CommentDialog(
            dialogType = state.commentDialogType ?: CommentDialogType.APPROVE,
            comment = state.comment,
            commentError = state.commentError,
            isProcessing = state.processingRequestId != null,
            onCommentChange = { viewModel.onCommentChange(it) },
            onConfirm = { viewModel.submitComment() },
            onDismiss = { viewModel.closeCommentDialog() }
        )
    }
}

@Composable
private fun LeaveRequestsTab(
    isLoading: Boolean,
    error: String?,
    requests: List<PendingLeaveRequest>,
    processingRequestId: Int?,
    onApprove: (Int) -> Unit,
    onReject: (Int) -> Unit,
    onRetry: () -> Unit
) {
    when {
        isLoading -> LoadingState()
        error != null -> ErrorState(message = error, onRetry = onRetry)
        requests.isEmpty() -> EmptyState(title = stringResource(R.string.manager_no_leave_requests))
        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(requests) { request ->
                    LeaveRequestCard(
                        request = request,
                        isProcessing = processingRequestId == request.id,
                        onApprove = { onApprove(request.id) },
                        onReject = { onReject(request.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaveRequestCard(
    request: PendingLeaveRequest,
    isProcessing: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (request.user.avatar != null) {
                    AsyncImage(
                        model = request.user.avatar,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = request.user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.manager_leave_type),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = when (request.leaveType) {
                            "vacation" -> stringResource(R.string.leave_type_vacation)
                            "sick" -> stringResource(R.string.leave_type_sick)
                            "personal" -> stringResource(R.string.leave_type_personal)
                            "unpaid" -> stringResource(R.string.leave_type_unpaid)
                            else -> request.leaveType
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.manager_period),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${DateUtils.formatDate(request.startDate)} - ${
                            DateUtils.formatDate(
                                request.endDate
                            )
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (!request.reason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.manager_reason),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = request.reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    enabled = !isProcessing,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.manager_reject))
                    }
                }

                Button(
                    onClick = onApprove,
                    enabled = !isProcessing,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.manager_approve))
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveEmployeesTab(
    isLoading: Boolean,
    error: String?,
    employees: List<ActiveEmployee>,
    onRetry: () -> Unit
) {
    when {
        isLoading -> LoadingState()
        error != null -> ErrorState(message = error, onRetry = onRetry)
        employees.isEmpty() -> EmptyState(title = stringResource(R.string.manager_no_active_employees))
        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(employees) { employee ->
                    ActiveEmployeeCard(employee = employee)
                }
            }
        }
    }
}

@Composable
private fun ActiveEmployeeCard(employee: ActiveEmployee) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (employee.user.avatar != null) {
                AsyncImage(
                    model = employee.user.avatar,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
            } else {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = employee.user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = employee.user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${stringResource(R.string.manager_clock_in)}: ${
                        DateUtils.formatTime(
                            employee.startTime
                        )
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!employee.locationData.isNullOrBlank()) {
                    Text(
                        text = "${stringResource(R.string.manager_location)}: ${employee.locationData}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (employee.stopTime == null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = stringResource(R.string.manager_active),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentDialog(
    dialogType: CommentDialogType,
    comment: String,
    commentError: String?,
    isProcessing: Boolean,
    onCommentChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (dialogType) {
                    CommentDialogType.APPROVE -> stringResource(R.string.manager_approve_title)
                    CommentDialogType.REJECT -> stringResource(R.string.manager_reject_title)
                }
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = when (dialogType) {
                        CommentDialogType.APPROVE -> stringResource(R.string.manager_approve_message)
                        CommentDialogType.REJECT -> stringResource(R.string.manager_reject_message)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                androidx.compose.material3.OutlinedTextField(
                    value = comment,
                    onValueChange = onCommentChange,
                    label = { Text(stringResource(R.string.manager_comment_label)) },
                    placeholder = { Text(stringResource(R.string.manager_comment_placeholder)) },
                    isError = commentError != null,
                    supportingText = commentError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = when (dialogType) {
                            CommentDialogType.APPROVE -> stringResource(R.string.manager_approve)
                            CommentDialogType.REJECT -> stringResource(R.string.manager_reject)
                        }
                    )
                }
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = onDismiss,
                enabled = !isProcessing
            ) {
                Text(stringResource(R.string.general_cancel))
            }
        }
    )
}
