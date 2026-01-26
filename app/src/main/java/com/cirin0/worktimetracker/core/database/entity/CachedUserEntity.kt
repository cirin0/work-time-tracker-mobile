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
    val companyId: Int?,
    val companyName: String?,
    val managerId: Int?,
    val managerName: String?,
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
        company = if (companyId != null && companyName != null) {
            Company(id = companyId, name = companyName)
        } else null,
        manager = if (managerId != null && managerName != null) {
            Manager(id = managerId, name = managerName)
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
        companyId = company?.id,
        companyName = company?.name,
        managerId = manager?.id,
        managerName = manager?.name,
        workScheduleId = workSchedule?.id,
        workScheduleName = workSchedule?.name,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
