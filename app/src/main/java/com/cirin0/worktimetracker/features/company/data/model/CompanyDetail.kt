package com.cirin0.worktimetracker.features.company.data.model

import com.google.gson.annotations.SerializedName

data class CompanyDetail(
    val id: Int,
    val name: String,
    val email: String?,
    val phone: String?,
    val logo: String?,
    val description: String?,
    val address: String?,
    val manager: CompanyManager,
    val employees: List<CompanyEmployee>,
    @SerializedName("users_count") val usersCount: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class CompanyManager(
    val id: Int,
    val name: String,
    val email: String
)

data class CompanyEmployee(
    val id: Int,
    val name: String,
    val email: String,
    val avatar: String?
)
