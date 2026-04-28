package com.cirin0.worktimetracker.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cirin0.worktimetracker.features.company.data.model.BaseUser
import com.cirin0.worktimetracker.features.company.data.model.CompanyDetail
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "cached_company")
data class CachedCompanyEntity(
    @PrimaryKey
    val cacheKey: Int = 1,
    val companyId: Int,
    val name: String,
    val email: String?,
    val phone: String?,
    val address: String?,
    val description: String?,
    val logo: String?,
    val latitude: String?,
    val longitude: String?,
    val radiusMeters: Int?,
    val latenessGraceMinutes: Int?,
    val overtimeThresholdHours: Double?,
    val managerId: Int,
    val managerName: String,
    val managerEmail: String,
    val managerAvatar: String?,
    val employeesJson: String?,
    val usersCount: Int,
    val createdAt: String,
    val updatedAt: String,
    val cachedAt: Long = System.currentTimeMillis()
)

private val gson = Gson()

fun CachedCompanyEntity.toCompanyDetail(): CompanyDetail {
    val employeesType = object : TypeToken<List<BaseUser>>() {}.type
    val employees = employeesJson?.let { gson.fromJson<List<BaseUser>>(it, employeesType) }

    return CompanyDetail(
        id = companyId,
        name = name,
        email = email,
        phone = phone,
        address = address,
        description = description,
        logo = logo,
        latitude = latitude,
        longitude = longitude,
        radiusMeters = radiusMeters,
        latenessGraceMinutes = latenessGraceMinutes,
        overtimeThresholdHours = overtimeThresholdHours,
        manager = BaseUser(
            id = managerId,
            name = managerName,
            email = managerEmail,
            avatar = managerAvatar
        ),
        employees = employees,
        usersCount = usersCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun CompanyDetail.toCachedEntity(): CachedCompanyEntity {
    return CachedCompanyEntity(
        cacheKey = 1,
        companyId = id,
        name = name,
        email = email,
        phone = phone,
        address = address,
        description = description,
        logo = logo,
        latitude = latitude,
        longitude = longitude,
        radiusMeters = radiusMeters,
        latenessGraceMinutes = latenessGraceMinutes,
        overtimeThresholdHours = overtimeThresholdHours,
        managerId = manager.id,
        managerName = manager.name,
        managerEmail = manager.email,
        managerAvatar = manager.avatar,
        employeesJson = employees?.let { gson.toJson(it) },
        usersCount = usersCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
