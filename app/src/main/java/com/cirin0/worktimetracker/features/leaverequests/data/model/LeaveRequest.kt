package com.cirin0.worktimetracker.features.leaverequests.data.model

import com.google.gson.annotations.SerializedName

enum class LeaveRequestType(val value: String) {
    SICK("sick"),
    VACATION("vacation"),
    PERSONAL("personal"),
    UNPAID("unpaid"),
    BUSINESS_TRIP("business_trip");

    companion object {
        fun fromValue(value: String): LeaveRequestType? {
            return entries.find { it.value == value }
        }
    }
}

enum class LeaveRequestStatus(val value: String) {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected");

    companion object {
        fun fromValue(value: String): LeaveRequestStatus? {
            return entries.find { it.value == value }
        }
    }
}

data class LeaveRequest(
    val id: Int,
    val user: UserInfo? = null,
    val type: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    val reason: String? = null,
    val status: String,
    val processor: UserInfo? = null,
    @SerializedName("manager_comment") val managerComment: String? = null,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String? = null
) {
    fun getTypeEnum(): LeaveRequestType =
        LeaveRequestType.fromValue(type) ?: LeaveRequestType.VACATION

    fun getStatusEnum(): LeaveRequestStatus =
        LeaveRequestStatus.fromValue(status) ?: LeaveRequestStatus.PENDING
}

data class UserInfo(
    val id: Int,
    val name: String,
    val email: String,
    val avatar: String?
)

data class LeaveRequestsResponse(
    val data: List<LeaveRequest>
)

data class CreateLeaveRequestRequest(
    val type: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    val reason: String
)

data class CreateLeaveRequestResponse(
    val message: String,
    val data: LeaveRequest
)

