package com.cirin0.worktimetracker.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cirin0.worktimetracker.features.leaverequests.data.model.LeaveRequest
import com.cirin0.worktimetracker.features.leaverequests.data.model.UserInfo

@Entity(tableName = "cached_leave_requests")
data class CachedLeaveRequestEntity(
    @PrimaryKey
    val id: Int,
    val userId: Int?,
    val userName: String?,
    val userEmail: String?,
    val userAvatar: String?,
    val type: String,
    val startDate: String,
    val endDate: String,
    val reason: String?,
    val status: String,
    val processorId: Int?,
    val processorName: String?,
    val processorEmail: String?,
    val processorAvatar: String?,
    val managerComment: String?,
    val createdAt: String,
    val updatedAt: String?,
    val cachedAt: Long = System.currentTimeMillis()
)

fun CachedLeaveRequestEntity.toLeaveRequest(): LeaveRequest {
    return LeaveRequest(
        id = id,
        user = if (userId != null && userName != null && userEmail != null) {
            UserInfo(
                id = userId,
                name = userName,
                email = userEmail,
                avatar = userAvatar
            )
        } else {
            null
        },
        type = type,
        startDate = startDate,
        endDate = endDate,
        reason = reason,
        status = status,
        processor = if (processorId != null && processorName != null && processorEmail != null) {
            UserInfo(
                id = processorId,
                name = processorName,
                email = processorEmail,
                avatar = processorAvatar
            )
        } else {
            null
        },
        managerComment = managerComment,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun LeaveRequest.toCachedEntity(): CachedLeaveRequestEntity {
    return CachedLeaveRequestEntity(
        id = id,
        userId = user?.id,
        userName = user?.name,
        userEmail = user?.email,
        userAvatar = user?.avatar,
        type = type,
        startDate = startDate,
        endDate = endDate,
        reason = reason,
        status = status,
        processorId = processor?.id,
        processorName = processor?.name,
        processorEmail = processor?.email,
        processorAvatar = processor?.avatar,
        managerComment = managerComment,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

