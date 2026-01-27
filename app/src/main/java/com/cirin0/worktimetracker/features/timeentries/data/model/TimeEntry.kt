package com.cirin0.worktimetracker.features.timeentries.data.model

import com.google.gson.annotations.SerializedName

data class TimeEntry(
    @SerializedName("id")
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("stop_time")
    val stopTime: String?,
    @SerializedName("duration")
    val duration: Int?,
    @SerializedName("start_comment")
    val startComment: String?,
    @SerializedName("stop_comment")
    val stopComment: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class TimeEntryRequest(
    @SerializedName("start_comment")
    val startComment: String? = null
)

data class StopTimeEntryRequest(
    @SerializedName("stop_comment")
    val stopComment: String? = null
)

data class TimeEntryResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: TimeEntry?
)

data class TimeEntriesListResponse(
    @SerializedName("data")
    val data: List<TimeEntry>
)
