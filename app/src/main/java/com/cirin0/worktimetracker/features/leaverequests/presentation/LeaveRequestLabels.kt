package com.cirin0.worktimetracker.features.leaverequests.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.cirin0.worktimetracker.R
import com.cirin0.worktimetracker.features.leaverequests.data.model.LeaveRequestStatus
import com.cirin0.worktimetracker.features.leaverequests.data.model.LeaveRequestType

@Composable
fun leaveRequestTypeLabel(type: LeaveRequestType): String {
    return when (type) {
        LeaveRequestType.SICK -> stringResource(R.string.leave_type_sick)
        LeaveRequestType.VACATION -> stringResource(R.string.leave_type_vacation)
        LeaveRequestType.PERSONAL -> stringResource(R.string.leave_type_personal)
        LeaveRequestType.UNPAID -> stringResource(R.string.leave_type_unpaid)
        LeaveRequestType.BUSINESS_TRIP -> stringResource(R.string.leave_type_business_trip)
    }
}

@Composable
fun leaveRequestStatusLabel(status: LeaveRequestStatus): String {
    return when (status) {
        LeaveRequestStatus.PENDING -> stringResource(R.string.leave_status_pending)
        LeaveRequestStatus.APPROVED -> stringResource(R.string.leave_status_approved)
        LeaveRequestStatus.REJECTED -> stringResource(R.string.leave_status_rejected)
    }
}

