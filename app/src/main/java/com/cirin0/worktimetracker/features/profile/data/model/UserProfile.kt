package com.cirin0.worktimetracker.features.profile.data.model

import com.google.gson.annotations.SerializedName

enum class WorkMode(val value: String) {
    REMOTE("remote"),
    OFFICE("office"),
    HYBRID("hybrid");

    companion object {
        fun fromString(value: String): WorkMode {
            return entries.find { it.value == value } ?: OFFICE
        }
    }
}

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val avatar: String?,
    @SerializedName("work_mode") val workMode: String,
    @SerializedName("has_pin_code") val hasPinCode: Boolean,
    val company: Company?,
    val manager: Manager?,
    @SerializedName("work_schedule") val workSchedule: WorkSchedule?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
) {
    fun getWorkModeEnum(): WorkMode = WorkMode.fromString(workMode)

    fun requiresGPS(): Boolean = getWorkModeEnum() == WorkMode.OFFICE

    fun isRemote(): Boolean = getWorkModeEnum() == WorkMode.REMOTE

    fun isHybrid(): Boolean = getWorkModeEnum() == WorkMode.HYBRID
}

data class Company(
    val id: Int,
    val name: String
)

data class Manager(
    val id: Int,
    val name: String,
    val email: String,
    val avatar: String?
)

data class WorkSchedule(
    val id: Int,
    val name: String
)
