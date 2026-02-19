package com.cirin0.worktimetracker.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cirin0.worktimetracker.features.profile.data.model.Company
import com.cirin0.worktimetracker.features.profile.data.model.Manager
import com.cirin0.worktimetracker.features.profile.data.model.User
import com.cirin0.worktimetracker.features.profile.data.model.WorkSchedule

@Entity(tableName = "cached_user")
data class CachedUserEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val avatar: String?,
    val workMode: String,
    val hasPinCode: Boolean,
    val companyId: Int?,
    val companyName: String?,
    val managerId: Int?,
    val managerName: String?,
    val managerEmail: String?,
    val managerAvatar: String?,
    val workScheduleId: Int?,
    val workScheduleName: String?,
    val createdAt: String,
    val updatedAt: String,
    val cachedAt: Long = System.currentTimeMillis()
)

fun CachedUserEntity.toUser(): User {
    return User(
        id = id,
        name = name,
        email = email,
        role = role,
        avatar = avatar,
        workMode = workMode,
        hasPinCode = hasPinCode,
        company = if (companyId != null && companyName != null) {
            Company(id = companyId, name = companyName)
        } else null,
        manager = if (managerId != null && managerName != null && managerEmail != null && avatar != null) {
            Manager(
                id = managerId,
                name = managerName,
                email = managerEmail,
                avatar = managerAvatar
            )
        } else null,
        workSchedule = if (workScheduleId != null && workScheduleName != null) {
            WorkSchedule(id = workScheduleId, name = workScheduleName)
        } else null,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun User.toCachedEntity(): CachedUserEntity {
    return CachedUserEntity(
        id = id,
        name = name,
        email = email,
        role = role,
        avatar = avatar,
        workMode = workMode,
        hasPinCode = hasPinCode,
        companyId = company?.id,
        companyName = company?.name,
        managerId = manager?.id,
        managerName = manager?.name,
        managerEmail = manager?.email,
        managerAvatar = manager?.avatar,
        workScheduleId = workSchedule?.id,
        workScheduleName = workSchedule?.name,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
