package com.cirin0.worktimetracker.features.company.data.model

import com.google.gson.annotations.SerializedName

data class CompanyDetail(
    val id: Int,
    val name: String,
    val email: String?,
    val phone: String?,
    val address: String?,
    val description: String?,
    val logo: String?,
    val latitude: String?,
    val longitude: String?,
    @SerializedName("radius_meters")
    val radiusMeters: Int?,
    val manager: BaseUser,
    val employees: List<BaseUser>?,
    @SerializedName("employee_count") val usersCount: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
) {
    fun getLatitudeAsDouble(): Double? = latitude?.toDoubleOrNull()

    fun getLongitudeAsDouble(): Double? = longitude?.toDoubleOrNull()

    fun getEmployeesList(): List<BaseUser> = employees ?: emptyList()
}

data class BaseUser(
    val id: Int,
    val name: String,
    val email: String,
    val avatar: String?
)
