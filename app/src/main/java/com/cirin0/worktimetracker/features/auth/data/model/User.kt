package com.cirin0.worktimetracker.features.auth.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val avatar: String?,
    val company: Company?,
    val manager: Manager?,
    @SerializedName("work_schedule") val workSchedule: WorkSchedule?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class Company(
    val id: Int,
    val name: String
)

data class Manager(
    val id: Int,
    val name: String
)

data class WorkSchedule(
    val id: Int,
    val name: String
)
