package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_punches")
data class PendingPunch(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // in or out
    val lat: Double,
    val lng: Double,
    val accuracy: Double?,
    val photoBase64: String?,
    val note: String?,
    val capturedAt: String,
    val idempotencyKey: String,
    val status: String = "QUEUED", // QUEUED, SENDING, FAILED
    val attempts: Int = 0,
    val lastError: String? = null
)
