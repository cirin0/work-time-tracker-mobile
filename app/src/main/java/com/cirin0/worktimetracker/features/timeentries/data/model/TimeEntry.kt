package com.cirin0.worktimetracker.features.timeentries.data.model

import com.google.gson.annotations.SerializedName

data class TimeEntry(
    @SerializedName("id")
    val id: Int,
    @SerializedName("user")
    val user: TimeEntryUser,
    @SerializedName("date")
    val date: String,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("stop_time")
    val stopTime: String?,
    @SerializedName("duration")
    val duration: Int?,
    @SerializedName("entry_type")
    val entryType: String,
    @SerializedName("location_data")
    val locationData: LocationData?,
    @SerializedName("start_comment")
    val startComment: String?,
    @SerializedName("stop_comment")
    val stopComment: String?,
    @SerializedName("lateness_minutes")
    val latenessMinutes: Int?,
    @SerializedName("scheduled_start_time")
    val scheduledStartTime: String?,
    @SerializedName("early_leave_minutes")
    val earlyLeaveMinutes: Int?,
    @SerializedName("scheduled_end_time")
    val scheduledEndTime: String?,
    @SerializedName("overtime_minutes")
    val overtimeMinutes: Int?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)

data class LocationData(
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lng")
    val lng: Double
)

data class TimeEntryRequest(
    @SerializedName("start_comment")
    val startComment: String? = null,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("qr_code")
    val qrCode: String?
)

data class StopTimeEntryRequest(
    @SerializedName("stop_comment")
    val stopComment: String? = null,
    @SerializedName("pin_code")
    val pinCode: String
)

data class TimeEntryResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: TimeEntry?
)

data class TimeEntriesListResponse(
    @SerializedName("data")
    val data: List<TimeEntry>,
    @SerializedName("links")
    val links: PaginationLinks?,
    @SerializedName("meta")
    val meta: PaginationMeta?
)

data class PaginatedTimeEntries(
    val data: List<TimeEntry>,
    val meta: PaginationMeta?
)

data class PaginationLinks(
    @SerializedName("first")
    val first: String?,
    @SerializedName("last")
    val last: String?,
    @SerializedName("prev")
    val prev: String?,
    @SerializedName("next")
    val next: String?
)

data class PaginationMeta(
    @SerializedName("current_page")
    val currentPage: Int,
    @SerializedName("from")
    val from: Int?,
    @SerializedName("last_page")
    val lastPage: Int,
    @SerializedName("links")
    val links: List<PaginationLink>,
    @SerializedName("path")
    val path: String,
    @SerializedName("per_page")
    val perPage: Int,
    @SerializedName("to")
    val to: Int?,
    @SerializedName("total")
    val total: Int
)

data class PaginationLink(
    @SerializedName("url")
    val url: String?,
    @SerializedName("label")
    val label: String,
    @SerializedName("active")
    val active: Boolean
)

data class TimeEntryUser(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String
)
