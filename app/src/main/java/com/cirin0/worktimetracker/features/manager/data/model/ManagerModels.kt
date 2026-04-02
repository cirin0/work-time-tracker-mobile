package com.cirin0.worktimetracker.features.manager.data.model

import com.google.gson.annotations.SerializedName

data class PendingLeaveRequestsResponse(
    val data: List<PendingLeaveRequest>
)

data class PendingLeaveRequest(
    val id: Int,
    val user: LeaveRequestUser,
    @SerializedName("type") val leaveType: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    val reason: String?,
    val status: String,
    @SerializedName("manager_comment") val managerComment: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String?,
    val processor: LeaveRequestUser?
)

data class LeaveRequestUser(
    val id: Int,
    val name: String,
    val email: String,
    val avatar: String?
)

data class ActiveEmployeesResponse(
    val data: List<ActiveEmployee>
)

data class ActiveEmployee(
    val id: Int,
    val user: EmployeeUser,
    val date: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("stop_time") val stopTime: String?,
    val duration: Double?,
    @SerializedName("entry_type") val entryType: String?,
    @SerializedName("location_data") val locationData: String?,
    @SerializedName("start_comment") val startComment: String?,
    @SerializedName("stop_comment") val stopComment: String?,
    @SerializedName("lateness_minutes") val latenessMinutes: Int?,
    @SerializedName("scheduled_start_time") val scheduledStartTime: String?,
    @SerializedName("early_leave_minutes") val earlyLeaveMinutes: Int?,
    @SerializedName("scheduled_end_time") val scheduledEndTime: String?,
    @SerializedName("overtime_minutes") val overtimeMinutes: Int?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class EmployeeUser(
    val id: Int,
    val name: String,
    val email: String,
    val avatar: String?
)

data class LeaveRequestActionRequest(
    @SerializedName("manager_comment") val managerComment: String? = null
)

data class LeaveRequestActionResponse(
    val message: String,
    val data: PendingLeaveRequest
)
