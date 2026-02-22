package com.cirin0.worktimetracker.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cirin0.worktimetracker.features.timeentries.data.model.LocationData
import com.cirin0.worktimetracker.features.timeentries.data.model.TimeEntry
import com.cirin0.worktimetracker.features.timeentries.data.model.TimeEntryUser

@Entity(tableName = "cached_time_entries")
data class CachedTimeEntryEntity(
    @PrimaryKey
    val id: Int,
    val userId: Int,
    val userName: String,
    val userEmail: String,
    val startTime: String,
    val stopTime: String?,
    val duration: Int?,
    val entryType: String,
    val locationLat: Double?,
    val locationLng: Double?,
    val startComment: String?,
    val stopComment: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val cachedAt: Long = System.currentTimeMillis()
)

fun CachedTimeEntryEntity.toTimeEntry(): TimeEntry {
    return TimeEntry(
        id = id,
        user = TimeEntryUser(
            id = userId,
            name = userName,
            email = userEmail
        ),
        startTime = startTime,
        stopTime = stopTime,
        duration = duration,
        entryType = entryType,
        locationData = if (locationLat != null && locationLng != null) {
            LocationData(lat = locationLat, lng = locationLng)
        } else null,
        startComment = startComment,
        stopComment = stopComment,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun TimeEntry.toCachedEntity(): CachedTimeEntryEntity {
    return CachedTimeEntryEntity(
        id = id,
        userId = user.id,
        userName = user.name,
        userEmail = user.email,
        startTime = startTime,
        stopTime = stopTime,
        duration = duration,
        entryType = entryType,
        locationLat = locationData?.lat,
        locationLng = locationData?.lng,
        startComment = startComment,
        stopComment = stopComment,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
